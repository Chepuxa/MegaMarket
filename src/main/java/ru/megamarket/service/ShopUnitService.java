package ru.megamarket.service;

import ru.megamarket.dto.ShopUnit;
import ru.megamarket.dto.ShopUnitImport;
import ru.megamarket.dto.ShopUnitStatisticResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public interface ShopUnitService {

    void importItems(List<ShopUnitImport> shopUnitImports, Instant updateDate);

    ShopUnit findItem(UUID id);

    void removeItem(UUID id);

    ShopUnitStatisticResponse findSales(Instant date);

    ShopUnitStatisticResponse getStatistic(UUID id, Instant dateStart, Instant dateEnd);
}
