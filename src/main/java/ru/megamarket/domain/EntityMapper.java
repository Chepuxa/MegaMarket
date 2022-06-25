package ru.megamarket.domain;

import org.mapstruct.Mapper;
import ru.megamarket.domain.statistic.ShopUnitStatisticEntity;
import ru.megamarket.domain.unit.ShopUnitEntity;
import ru.megamarket.dto.ShopUnit;
import ru.megamarket.dto.ShopUnitImport;
import ru.megamarket.dto.ShopUnitStatisticUnit;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper
public interface EntityMapper {

    EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

    ShopUnitEntity unitDtoToUnitEntity(ShopUnitImport shopUnitDto);

    ShopUnit unitEntityToUnitDto(ShopUnitEntity shopUnitEntity);

    Set<ShopUnitEntity> unitDtoSetToUnitEntitySet(List<ShopUnitImport> shopUnitDtoList);

    Set<ShopUnitStatisticUnit> unitEntitySetToStatisticDtoSet(Set<ShopUnitEntity> shopUnitEntitySet);

    Set<ShopUnitStatisticEntity> unitEntitySetToStatisticEntitySet(Set<ShopUnitEntity> shopUnitEntitySet);

    Set<ShopUnitStatisticUnit> statisticEntitySetToStatisticUnitSet(Set<ShopUnitStatisticEntity> shopUnitEntitySet);
}