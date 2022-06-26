package ru.megamarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.megamarket.domain.unit.ShopUnitEntity;
import ru.megamarket.domain.unit.ShopUnitsRepo;
import ru.megamarket.dto.ShopUnitType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ShopUnitServiceUtils {

    protected final ShopUnitsRepo shopUnitsRepo;

    /**
     * Разбивает переданные в запросе деревья элементов, представленных списком с неопределенной глубиной,
     * на упорядоченные списки элементов.
     * Список упорядочен сверху вниз и будет разложен следующим образом:
     * Cat1(Cat2(Cat3(Off1))), Cat4(Cat5(Off2))  -> Cat1, Cat2, Cat3, Off1, Cat4, Cat5, Off2,
     * родительский элемент всегда находится левее дочернего элемента.
     *
     * @param shopUnitsForests список элементов
     * @return упорядоченный по иерархии список элементов
     */
    public LinkedHashMap<UUID, ShopUnitEntity> iterativeSort(Set<ShopUnitEntity> shopUnitsForests) {
        LinkedHashMap<UUID, ShopUnitEntity> sortedMap = new LinkedHashMap<>();
        shopUnitsForests.forEach(forest -> {
            Stack<ShopUnitEntity> toVisit = new Stack<>();
            toVisit.push(forest);
            while (!toVisit.isEmpty()) {
                ShopUnitEntity node = toVisit.pop();
                if (node.getChildren() != null) {
                    node.getChildren().forEach(toVisit::push);
                }
                sortedMap.put(node.getId(), node);
            }
        });
        return sortedMap;
    }

    /**
     * Создает деревья из переданных элементов на основе их взаимоотношений, обозначенных полем parentId,
     * пример: Cat1(parentId: null), Cat2(parentId:Cat1), Offer1(parentId:Cat2) -> Cat1(Cat2(Offer1)).
     *
     * @param nodes HashMap идентификаторов (ключей) и соответствующих им элементов (значений)
     * @return набор созданных деревьев (наборов с глубиной)
     */
    public Set<ShopUnitEntity> createForest(Map<UUID, ShopUnitEntity> nodes) {
        Set<ShopUnitEntity> forest = new HashSet<>();
        for (ShopUnitEntity shopUnitEntity : nodes.values()) {
            UUID id = shopUnitEntity.getId();
            UUID parentId = shopUnitEntity.getParentId();
            ShopUnitEntity node = nodes.get(id);
            ShopUnitEntity parent = nodes.getOrDefault(parentId, null);
            if (parentId == null || parent == null) {
                forest.add(node);
            } else {
                if (parent.getChildren() == null) {
                    parent.setChildren(new HashSet<>());
                }
                parent.getChildren().add(node);
                nodes.put(parentId, parent);
            }
        }
        return forest;
    }

    /**
     * Для каждого элемента типа CATEGORY устанавливает поле price, равное среднему значению поля price
     * всех элементов типа OFFER, которые встречаются ниже по иерархии.
     * Если цена элемента типа CATEGORY была изменена, этот элемент попадает в результирующий набор.
     *
     * @param sortedTree набор элементов, отсортированный по иерархии
     * @return набор элементов типа CATEGORY, для которых было изменено поле price
     */
    public Map<UUID, ShopUnitEntity> computeCategoryPrices(LinkedHashMap<UUID, ShopUnitEntity> sortedTree) {
        Map<UUID, Long> childrenCount = new HashMap<>();
        Map<UUID, Long> childrenPrice = new HashMap<>();
        Map<UUID, ShopUnitEntity> updatedCategories = new HashMap<>();
        List<ShopUnitEntity> reversed = new ArrayList<>(sortedTree.values());
        Collections.reverse(reversed);
        reversed.forEach(shopUnitEntity -> {
            ShopUnitEntity entityToUpdate = sortedTree.get(shopUnitEntity.getId());
            long offersToIncrement = 0L;
            long priceToIncrement = 0L;
            if (shopUnitEntity.getType() == ShopUnitType.CATEGORY) {
                Long childCount = childrenCount.getOrDefault(shopUnitEntity.getId(), null);
                Long price = childrenPrice.getOrDefault(shopUnitEntity.getId(), null);
                if (childCount != null && !price.equals(0L)) {
                    Long newPrice = price / childCount;
                    if (entityToUpdate.getPrice() != null) {
                        if (!entityToUpdate.getPrice().equals(newPrice)) {
                            entityToUpdate.setPrice(newPrice);
                            sortedTree.put(entityToUpdate.getId(), entityToUpdate);
                            updatedCategories.put(shopUnitEntity.getId(), entityToUpdate);
                        }
                    } else {
                        entityToUpdate.setPrice(newPrice);
                        sortedTree.put(entityToUpdate.getId(), entityToUpdate);
                        updatedCategories.put(shopUnitEntity.getId(), entityToUpdate);
                    }
                    offersToIncrement = childCount;
                    priceToIncrement = price;
                } else {
                    if (entityToUpdate.getPrice() != null) {
                        updatedCategories.put(shopUnitEntity.getId(), entityToUpdate);
                    }
                    sortedTree.get(shopUnitEntity.getId()).setPrice(null);
                }
            } else {
                priceToIncrement = shopUnitEntity.getPrice();
                offersToIncrement = 1L;
            }
            if (shopUnitEntity.getParentId() != null) {
                childrenCount.merge(shopUnitEntity.getParentId(), offersToIncrement, Long::sum);
                childrenPrice.merge(shopUnitEntity.getParentId(), priceToIncrement, Long::sum);
            }
        });
        return updatedCategories;
    }

    /**
     * Проверяет, различаются ли значения полей type у элементов с одинаковыми id из переданных наборов.
     *
     * @param setOfEntitiesToCompareType  список элементов для сравнения
     * @param mapOfEntitiesToCompareType HashMap идентификаторов (ключей) и соответствующих им элементов (значений) для сравнения
     * @return true если различия найдены, иначе false
     */
    public boolean checkIfTypeChanged(Set<ShopUnitEntity> setOfEntitiesToCompareType,
                                      Map<UUID, ShopUnitEntity> mapOfEntitiesToCompareType) {
        return setOfEntitiesToCompareType.stream()
                .anyMatch(shopUnit -> !shopUnit.getType().equals(mapOfEntitiesToCompareType.get(shopUnit.getId()).getType()));
    }

    /**
     * Собирает все значения parentId элементов в наборе.
     *
     * @param shopUnitEntities  список элементов
     * @return набор UUID
     */
    public Set<UUID> collectParentIds(Set<ShopUnitEntity> shopUnitEntities) {
        Set<UUID> parentIds = new HashSet<>();
        shopUnitEntities.forEach(shopUnitEntity -> {
            UUID parentId = shopUnitEntity.getParentId();
            if (parentId != null) {
                parentIds.add(parentId);
            }
        });
        return parentIds;
    }

    /**
     * Собирает все значения parentId элементов в наборе, если это значение не равно значению id любого другого элемента в наборе.
     *
     * @param shopUnitEntities  список элементов
     * @return набор UUID
     */
    public Set<UUID> collectParentIdsWithoutReference(Map<UUID, ShopUnitEntity> shopUnitEntities) {
        Set<UUID> parentIds = new HashSet<>();
        shopUnitEntities.values().forEach(shopUnitEntity -> {
            UUID parentId = shopUnitEntity.getParentId();
            if (parentId != null && !shopUnitEntities.containsKey(parentId)) {
                parentIds.add(parentId);
            }
        });
        return parentIds;
    }

    /**
     * Проверяет, если в списке элементов тип любого элемента соответствует типу OFFER
     *
     * @param shopUnitEntities список элементов
     * @return true если в списке присутствует удовлетворяющий элемент, иначе false
     */
    public boolean checkIfAnyParentIsOffer(Set<ShopUnitEntity> shopUnitEntities) {
        return shopUnitEntities.stream().anyMatch(parent -> parent.getType() == ShopUnitType.OFFER);
    }

    /**
     * Создает набор, являющийся результатом слияния suiteToUpdateForm с набором suiteToUpdate,
     * при совпадении ключей приоритет у элемента из набора suiteToUpdateFrom.
     *
     * @param suiteToUpdate набор, с которым происходит слияние
     * @param suiteToUpdateFrom источник слияния
     * @return результат слияния
     */
    public LinkedHashMap<UUID, ShopUnitEntity> mergeEntitiesMaps(LinkedHashMap<UUID, ShopUnitEntity> suiteToUpdate,
                                                                 LinkedHashMap<UUID, ShopUnitEntity> suiteToUpdateFrom) {
        LinkedHashMap<UUID, ShopUnitEntity> comparedHasMap = new LinkedHashMap<>();
        suiteToUpdate.values().forEach(shopUnitEntity -> {
            ShopUnitEntity entityToPut = suiteToUpdateFrom.getOrDefault(shopUnitEntity.getId(), shopUnitEntity);
            comparedHasMap.put(shopUnitEntity.getId(), entityToPut);
        });
        suiteToUpdateFrom.values().forEach(shopUnitEntity -> comparedHasMap.putIfAbsent(shopUnitEntity.getId(), shopUnitEntity));
        return comparedHasMap;
    }

    /**
     * Находит для каждого переданного идентификатора родительское дерево и собирает в упорядоченный список,
     * список упорядочен снизу вверх и будет разложен следующим образом:
     * Cat1(Cat2(Cat3(Off1))), Cat4(Cat5(Off2)) -> Cat3, Cat2, Cat1, Cat5, Cat4.
     *
     * @param ids идентификаторы элементов
     * @return родительское дерево в виде отсортированного списка элементов
     */
    public LinkedList<ShopUnitEntity> unwrapParentsTree(Set<UUID> ids) {
        LinkedList<ShopUnitEntity> sortedUnwrappedParentsTree = new LinkedList<>();
        ids.forEach(parentId -> sortedUnwrappedParentsTree.addAll(shopUnitsRepo.getParentTreeById(parentId)));
        return sortedUnwrappedParentsTree;
    }

    /**
     * Находит все элементы, у которых id соответствуют parentId любого другого элемента в shopUnitsMap.
     *
     * @param shopUnitsMap HashMap идентификаторов (ключей) и соответствующих им элементов (значений)
     * @return список подходящих элементов
     */
    public Set<ShopUnitEntity> collectParentsFromRequest(Map<UUID, ShopUnitEntity> shopUnitsMap) {
        Set<ShopUnitEntity> parentsFromRequest = new HashSet<>();
        shopUnitsMap.values().forEach(shopUnitEntity -> {
            Optional<ShopUnitEntity> parentFromRequest = Optional.ofNullable(shopUnitsMap.get(shopUnitEntity.getParentId()));
            parentFromRequest.ifPresent(parentsFromRequest::add);
        });
        return parentsFromRequest;
    }

    /**
     * Если элемент из набора с типом CATEGORY уже присутствует в базе, добавляем к нему его детей из базы
     *
     * @param shopUnitEntitiesFromDb набор элементов из базы
     * @param shopUnitEntities набор элементов из запроса
     */
    public void addChildren(Set<ShopUnitEntity> shopUnitEntitiesFromDb, Map<UUID, ShopUnitEntity> shopUnitEntities) {
        shopUnitEntitiesFromDb.forEach(shopUnitEntity -> {
            if (shopUnitEntity.getType().equals(ShopUnitType.CATEGORY) && shopUnitEntity.getChildren() != null) {
                shopUnitEntities.get(shopUnitEntity.getId()).setChildren(shopUnitEntity.getChildren());
            }
        });
    }

    /**
     * Находит элементы типа OFFER в базе данных, дата которых находится в промежутке [endTime - saleRange(hours), endTime].
     *
     * @param endTime конец временного промежутка
     * @param saleRange указатель начала временного промежутка (начало = конец - saleRange)
     * @return список входящих во временной промежуток элементов типа OFFER
     */
    public Set<ShopUnitEntity> getSales(Instant endTime, Integer saleRange) {
        Instant startTime = endTime.minus(saleRange, ChronoUnit.HOURS);
        return shopUnitsRepo.getAllBetweenDates(startTime, endTime, ShopUnitType.OFFER.value());
    }
}
