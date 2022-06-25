package ru.megamarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum ShopUnitType {

    CATEGORY("CATEGORY"),
    OFFER("OFFER");

    private final String type;

    ShopUnitType(String type) {
        this.type = type;
    }

    public String value() {
        return type;
    }


}
