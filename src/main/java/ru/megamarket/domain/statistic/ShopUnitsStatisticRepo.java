package ru.megamarket.domain.statistic;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.megamarket.domain.JdbcQueries;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public interface ShopUnitsStatisticRepo  extends CrudRepository<ShopUnitStatisticEntity, UUID>, JdbcQueries {

    @Query(value = "select * from shop_unit_statistic where" +
            " (date >= :startDate OR cast(:startDate as date) is null)" +
            " and (date < :endDate OR cast(:endDate as date) is null)" +
            " and id = :id", nativeQuery = true)
    Set<ShopUnitStatisticEntity> getStatisticBetweenDates(@Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate,
                                           @Param("id") UUID id);

}
