package ru.megamarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.megamarket.domain.EntityMapper;
import ru.megamarket.domain.statistic.ShopUnitStatisticEntity;
import ru.megamarket.domain.statistic.ShopUnitsStatisticRepo;
import ru.megamarket.domain.unit.ShopUnitEntity;
import ru.megamarket.domain.unit.ShopUnitsRepo;
import ru.megamarket.dto.ShopUnit;
import ru.megamarket.dto.ShopUnitImport;
import ru.megamarket.dto.ShopUnitStatisticResponse;
import ru.megamarket.dto.ShopUnitStatisticUnit;
import ru.megamarket.dto.ShopUnitType;
import ru.megamarket.exceptions.IdShouldBeUniqueException;
import ru.megamarket.exceptions.ItemNotFoundException;
import ru.megamarket.exceptions.ParentShouldBeCategoryException;
import ru.megamarket.exceptions.ParentWasNotFoundException;
import ru.megamarket.exceptions.ShopUnitTypeNotUpdatableException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopUnitServiceImpl implements ShopUnitService {

    private final ShopUnitServiceUtils shopUnitServiceUtils;
    private final ShopUnitsStatisticRepo shopUnitsStatisticRepo;
    private final ShopUnitsRepo shopUnitsRepo;

    /**
     * Количество часов с начала распродажи
     */
    private final static int SALE_RANGE_IN_HOURS = 24;

    /**
     * Импорт новых товаров и/или категорий
     *
     * @param shopUnitImports элементы для импорта
     * @param updateDate      дата импорта
     */
    @Override
    public void importItems(List<ShopUnitImport> shopUnitImports, Instant updateDate) {
        if (shopUnitImports.isEmpty()) {
            return;
        }
        /* Пул parentId, который будет содержать parentId элементов из запроса и parentId тех же элементов из базы,
         * если они там присутствуют */
        Set<UUID> mixedParentIds = new HashSet<>();

        Set<ShopUnitEntity> shopUnitEntities = EntityMapper.INSTANCE.unitDtoSetToUnitEntitySet(shopUnitImports);

        /* Собираем все элементы из запроса в HashMap, где id элемента = этому элементу.
         * Из полученного набора проверяем, содержались ли в изначальном наборе повторяющиеся id */
        Map<UUID, ShopUnitEntity> shopUnitsMap = shopUnitEntities.stream().collect(Collectors.toMap(ShopUnitEntity::getId, v -> v));
        if (shopUnitsMap.size() != shopUnitImports.size()) {
            throw new IdShouldBeUniqueException();
        }

        /* Получаем список родительских элементов из запроса и список parentId которые не указывают на элемент из запроса */
        Set<ShopUnitEntity> parents = shopUnitServiceUtils.collectParentsFromRequest(shopUnitsMap);
        Set<UUID> parentIdsNotInRequest = shopUnitServiceUtils.collectParentIdsWithoutReference(shopUnitsMap);

        /* Если есть такие parentId, которые не указывают на элемент из запроса, проверяем,
         * что соответствующие им элементы есть в базе данных. Полученный набор элементов из базы и изначальный набор parentId
         * добавляем в пул всех родительских элементов и в пул перемешанных parentId соответственно */
        if (parentIdsNotInRequest != null) {
            Set<ShopUnitEntity> parentsFromDb = shopUnitsRepo.findByIdIn(parentIdsNotInRequest);
            if (parentsFromDb.size() != parentIdsNotInRequest.size()) {
                throw new ParentWasNotFoundException();
            }
            parents.addAll(parentsFromDb);
            mixedParentIds.addAll(parentIdsNotInRequest);
        }

        /* Получаем элементы которые уже есть в базе для последующей проверки их на изменение типа и для получения их parentId */
        Set<ShopUnitEntity> entitiesBeforeUpdate = shopUnitsRepo.findByIdIn(shopUnitsMap.keySet());
        if (shopUnitServiceUtils.checkIfTypeChanged(entitiesBeforeUpdate, shopUnitsMap)) {
            throw new ShopUnitTypeNotUpdatableException();
        }

        /* Добавляем категориям из запроса, которые уже есть в дб, детей */
        shopUnitServiceUtils.addChildren(entitiesBeforeUpdate, shopUnitsMap);

        /* Для тех элементов, которые уже есть в базе, берем значение поля parentId элемента из базы и добавляем в общий пул */
        Set<UUID> entitiesParentIdsBeforeUpdate = shopUnitServiceUtils.collectParentIds(entitiesBeforeUpdate);
        if (entitiesParentIdsBeforeUpdate != null) {
            mixedParentIds.addAll(entitiesParentIdsBeforeUpdate);
        }

        /* Строим дерево для элементов из запроса и разбиваем его в отсортированный набор (родитель всегда левее детей) */
        Set<ShopUnitEntity> shopUnitsForests = shopUnitServiceUtils.createForest(shopUnitsMap);
        LinkedHashMap<UUID, ShopUnitEntity> shopUnitEntitiesToUpsert = new LinkedHashMap<>(shopUnitServiceUtils.iterativeSort(shopUnitsForests));

        /*
         * Если в базе есть элементы с таким значением parentId, что оно будет изменено после вставления туда элементов из запроса,
         * то необходимо составить для таких элементов дерево иерархии и добавить все элементы выше по иерархии в набор для
         * последующего вставления в базу
         */
        if (!mixedParentIds.isEmpty()) {
            Map<UUID, ShopUnitEntity> dbShopUnitsMap = new HashMap<>();
            Set<ShopUnitEntity> unwrappedParentsTree = new HashSet<>(shopUnitServiceUtils.unwrapParentsTree(mixedParentIds));
            unwrappedParentsTree.forEach(parent -> dbShopUnitsMap.put(parent.getId(), parent));
            Set<ShopUnitEntity> dbShopUnitsForests = shopUnitServiceUtils.createForest(dbShopUnitsMap);
            LinkedHashMap<UUID, ShopUnitEntity> dbShopUnitEntitiesSorted = new LinkedHashMap<>(shopUnitServiceUtils.iterativeSort(dbShopUnitsForests));
            shopUnitEntitiesToUpsert = shopUnitServiceUtils.mergeEntitiesMaps(dbShopUnitEntitiesSorted, shopUnitEntitiesToUpsert);
        }

        if (shopUnitServiceUtils.checkIfAnyParentIsOffer(parents)) {
            throw new ParentShouldBeCategoryException();
        }

        /*
         * Рассчитываем поле price для категорий. Если у категории поле price поменялось, добавляем в результирующий набор.
         */
        Map<UUID, ShopUnitEntity> entitiesToUpdateDate = shopUnitServiceUtils.computeCategoryPrices(shopUnitEntitiesToUpsert);

        /*
         * К HashMap элементов из запроса добавляем HashMap элементов из базы, у которых обновится хотя бы одно поле
         * вставки в базу элементов из запроса.
         * Все элементы из полученного набора должны попасть в таблицу ShopUnitStatistic,
         * также у таких элементов должно обновиться поле date
         */
        shopUnitsMap.putAll(entitiesToUpdateDate);
        Set<ShopUnitEntity> toAddStats = new HashSet<>();
        shopUnitEntitiesToUpsert.values().forEach(shopUnitEntity -> {
            if (shopUnitsMap.getOrDefault(shopUnitEntity.getId(), null) != null) {
                shopUnitEntity.setDate(Timestamp.from(updateDate));
                toAddStats.add(shopUnitEntity);
            }
        });
        Set<ShopUnitStatisticEntity> statisticEntitiesToUpsert =
                EntityMapper.INSTANCE.unitEntitySetToStatisticEntitySet(toAddStats);
        shopUnitsRepo.shopUnitBatchUpsert(shopUnitEntitiesToUpsert.values(), 1000);
        shopUnitsStatisticRepo.shopUnitStatisticBatchUpsert(statisticEntitiesToUpsert, 1000);
    }

    /**
     * Получение информации об элементе по идентификатору.
     *
     * @param id идентификатор элемента
     * @return найденный элемент
     * @throws ItemNotFoundException если элемент отсутствует в базе
     */
    @Override
    public ShopUnit findItem(UUID id) {
        Optional<ShopUnitEntity> shopUnit = shopUnitsRepo.findById(id);
        if (shopUnit.isPresent()) {
            return EntityMapper.INSTANCE.unitEntityToUnitDto(shopUnit.get());
        } else {
            throw new ItemNotFoundException();
        }
    }

    /**
     * Удалить элемент по идентификатору.
     *
     * @param id идентификатор элемента
     * @throws ItemNotFoundException если элемент отсутствует в базе
     */
    @Override
    public void removeItem(UUID id) {
        Optional<ShopUnitEntity> shopUnit = shopUnitsRepo.findById(id);
        /*
         * Получаем дерево иерархии для удаляемого элемента.
         * Для элементов типа CATEGORY находящихся выше по иерархии рассчитываем поле price, и те элементы, у которых поле
         * изменилось, добавляем в таблицу со статистикой.
         */
        if (shopUnit.isPresent()) {
            Set<UUID> dbParentIdSet = new HashSet<>();
            dbParentIdSet.add(shopUnit.get().getParentId());
            Map<UUID, ShopUnitEntity> dbShopUnitsMap = new HashMap<>();
            Set<ShopUnitEntity> unwrappedParentsTree = new HashSet<>(shopUnitServiceUtils.unwrapParentsTree(dbParentIdSet));
            unwrappedParentsTree.forEach(parent -> dbShopUnitsMap.put(parent.getId(), parent));
            Set<ShopUnitEntity> dbShopUnitsForests = shopUnitServiceUtils.createForest(dbShopUnitsMap);
            LinkedHashMap<UUID, ShopUnitEntity> dbShopUnitEntitiesSorted =
                    new LinkedHashMap<>(shopUnitServiceUtils.iterativeSort(dbShopUnitsForests));
            dbShopUnitEntitiesSorted.remove(id);
            Map<UUID, ShopUnitEntity> entitiesToUpdateDate = shopUnitServiceUtils.computeCategoryPrices(dbShopUnitEntitiesSorted);
            Set<ShopUnitEntity> entitiesToKeepStatsOf = new HashSet<>(entitiesToUpdateDate.values());
            Set<ShopUnitStatisticEntity> statisticEntitiesToUpsert =
                    EntityMapper.INSTANCE.unitEntitySetToStatisticEntitySet(entitiesToKeepStatsOf);
            shopUnitsRepo.deleteById(id);
            shopUnitsStatisticRepo.shopUnitStatisticBatchUpsert(statisticEntitiesToUpsert, 1000);
        } else {
            throw new ItemNotFoundException();
        }
    }

    /**
     * Получение списка товаров, цена которых была обновлена в промежутке [dateEnd - SALES_RANGE_IN_HOURS, dateEnd].
     *
     * @param dateEnd предел временного промежутка (включительно)
     * @return ShopUnitStatisticResponse содержащий в себе набор ShopUnitStatisticUnit - элементов, входящих во временной промежуток
     */
    @Override
    public ShopUnitStatisticResponse findSales(Instant dateEnd) {
        Set<ShopUnitEntity> shopUnitsOnSale = shopUnitServiceUtils.getSales(dateEnd, SALE_RANGE_IN_HOURS);
        Set<ShopUnitStatisticUnit> mappedShopUnitsOnSale = EntityMapper.INSTANCE.unitEntitySetToStatisticDtoSet(shopUnitsOnSale);
        return new ShopUnitStatisticResponse(mappedShopUnitsOnSale);
    }

    /**
     * Получение статистики (истории обновлений) по товару/категории за заданный полуинтервал [dateStart, dateEnd).
     *
     * @param id        идентификатор элемента
     * @param dateStart начало промежутка (включительно)
     * @param dateEnd   конец промежутка
     * @return ShopUnitStatisticResponse содержащий в себе набор ShopUnitStatisticUnit - элементов, входящих во временной промежуток
     * @throws ItemNotFoundException если элемент отсутствует в базе
     */
    @Override
    public ShopUnitStatisticResponse getStatistic(UUID id, Instant dateStart, Instant dateEnd) {
        if (!shopUnitsRepo.existsById(id)) {
            throw new ItemNotFoundException();
        }
        Set<ShopUnitStatisticEntity> shopUnitsOnSale = shopUnitsStatisticRepo.getStatisticBetweenDates(dateStart, dateEnd, id);
        Set<ShopUnitStatisticUnit> mappedShopUnitsOnSale = EntityMapper.INSTANCE.statisticEntitySetToStatisticUnitSet(shopUnitsOnSale);
        return new ShopUnitStatisticResponse(mappedShopUnitsOnSale);
    }
}
