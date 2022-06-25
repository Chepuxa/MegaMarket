package ru.megamarket.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.megamarket.domain.statistic.ShopUnitStatisticEntity;
import ru.megamarket.domain.unit.ShopUnitEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

@RequiredArgsConstructor
public class JdbcQueriesImpl implements JdbcQueries {

    @PersistenceContext
    private EntityManager em;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int[][] shopUnitBatchUpsert(Collection<ShopUnitEntity> shopUnitEntities, int batchSize) {
        return jdbcTemplate.batchUpdate(
                "insert into shop_unit (id, date, name, parent_id, price, type) values(?,?,?,?,?,?)" +
                        " on conflict (id) do update set" +
                        " date = excluded.date," +
                        " name = excluded.name," +
                        " parent_id = excluded.parent_id," +
                        " price = excluded.price," +
                        " type = excluded.type",
                shopUnitEntities,
                batchSize,
                (ps, argument) -> {
                    ps.setObject(1, argument.getId());
                    ps.setObject(2, argument.getDate());
                    ps.setObject(3, argument.getName());
                    ps.setObject(4, argument.getParentId());
                    ps.setObject(5, argument.getPrice());
                    ps.setObject(6, argument.getType().value());
                });
    }

    @Override
    public int[][] shopUnitStatisticBatchUpsert(Collection<ShopUnitStatisticEntity> shopUnitStatisticEntities, int batchSize) {
        return jdbcTemplate.batchUpdate(
                "insert into shop_unit_statistic (key, id, date, name, parent_id, price, type)" +
                        " values (nextval('SUS_SEQ'),?,?,?,?,?,?)",
                shopUnitStatisticEntities,
                batchSize,
                (ps, argument) -> {
                    ps.setObject(1, argument.getId());
                    ps.setObject(2, argument.getDate());
                    ps.setObject(3, argument.getName());
                    ps.setObject(4, argument.getParentId());
                    ps.setObject(5, argument.getPrice());
                    ps.setObject(6, argument.getType().value());
                });
    }
}
