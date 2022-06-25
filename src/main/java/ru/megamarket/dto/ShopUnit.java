package ru.megamarket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.megamarket.openapi.OpenApiExamples;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(example = OpenApiExamples.UNIT)
public class ShopUnit {

    @Schema(description = "Уникальный идентфикатор", example = OpenApiExamples.ID, required = true)
    private UUID id;

    @Schema(description = "Имя элемента", required = true)
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Время последнего обновления элемента", example = OpenApiExamples.DATE, required = true)
    private Timestamp date;

    @Schema(description = "UUID родительской категории", example = OpenApiExamples.ID, nullable = true)
    private UUID parentId;

    @Schema(description = "Тип элемента - категория или товар", required = true)
    private ShopUnitType type;

    @Schema(nullable = true,
            description = "Целое число, для категории - это средняя цена всех дочерних товаров(включая товары подкатегорий)." +
                    " Если цена является не целым числом, округляется в меньшую сторону до целого числа." +
                    " Если категория не содержит товаров цена равна null.")
    private Long price;

    @Schema(description = "Список всех дочерних товаров\\категорий. Для товаров поле равно null.")
    private Set<ShopUnit> children;

    public Set<ShopUnit> getChildren() {
        if (type.equals(ShopUnitType.OFFER)) {
            return null;
        } else {
            return children;
        }
    }
}
