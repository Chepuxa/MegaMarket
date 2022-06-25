package ru.megamarket.domain.unit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.megamarket.domain.JdbcQueries;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public interface ShopUnitsRepo extends CrudRepository<ShopUnitEntity, UUID>, JdbcQueries {

    @Query(value = "select * from shop_unit where id in :ids", nativeQuery = true)
    Set<ShopUnitEntity> findByIdIn(@Param("ids") Set<UUID> ids);

    @Query(value = "with recursive subordinates as (" +
            " select shopunit.date, shopunit.id, shopunit.name, shopunit.type, shopunit.price, shopunit.parent_id from shop_unit shopunit where shopunit.id = :id" +
            " union" +
            " select shopunit2.date, shopunit2.id, shopunit2.name, shopunit2.type, shopunit2.price, shopunit2.parent_id from shop_unit shopunit2" +
            " join subordinates on shopunit2.id = subordinates.parent_id)" +
            " select * from subordinates", nativeQuery = true)
    Set<ShopUnitEntity> getParentTreeById(@Param("id") UUID id);

    @Query(value = "select * from shop_unit where (date between :startDate AND :endDate) and type = :type", nativeQuery = true)
    Set<ShopUnitEntity> getAllBetweenDates(@Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate,
                                           @Param("type") String type);

    boolean existsById(UUID ids);

    @Transactional
    void deleteById(@Param(value = "id") UUID id);

}