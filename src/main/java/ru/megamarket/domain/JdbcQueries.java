package ru.megamarket.domain;

import ru.megamarket.domain.statistic.ShopUnitStatisticEntity;
import ru.megamarket.domain.unit.ShopUnitEntity;

import java.util.Collection;

public interface JdbcQueries {

    int[][] shopUnitBatchUpsert(Collection<ShopUnitEntity> shopUnitEntities, int batchSize);

    int[][] shopUnitStatisticBatchUpsert(Collection<ShopUnitStatisticEntity> shopUnitStatisticEntitiesEntities, int batchSize);
}
