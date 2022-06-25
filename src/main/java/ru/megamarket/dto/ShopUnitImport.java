package ru.megamarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.megamarket.exceptions.constraints.CategoryPriceConstraint;
import ru.megamarket.openapi.OpenApiExamples;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@CategoryPriceConstraint
@Schema(example = OpenApiExamples.IMPORT)
public class ShopUnitImport {

    @NotNull
    @Schema(description = "Уникальный идентификатор", example = OpenApiExamples.ID, required = true)
    private UUID id;

    @Schema(description = "Имя элемента")
    @NotNull
    private String name;

    @Schema(description = "UUID родительской категории", example = OpenApiExamples.ID, nullable = true)
    private UUID parentId;

    @NotNull
    @Schema(description = "Тип элемента - категория или товар", required = true)
    private ShopUnitType type;

    @Schema(description = "Целое число, для категорий поле должно содержать null.", nullable = true)
    private Long price;
}
