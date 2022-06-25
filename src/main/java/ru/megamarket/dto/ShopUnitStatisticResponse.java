package ru.megamarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ShopUnitStatisticResponse {

    @Schema(description = "История в произвольном порядке")
    private Set<ShopUnitStatisticUnit> items;
}
