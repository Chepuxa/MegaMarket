package ru.megamarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.megamarket.openapi.OpenApiExamples;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
public class ShopUnitImportRequest {

    @Valid
    @Schema(description = "Импортируемые элементы")
    private List<ShopUnitImport> items;

    @NotNull
    @Schema(description = "Время обновления добавляемых товаров/категорий", example = OpenApiExamples.DATE)
    private Instant updateDate;
}
