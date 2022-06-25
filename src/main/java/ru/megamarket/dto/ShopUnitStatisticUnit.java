package ru.megamarket.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.megamarket.openapi.OpenApiExamples;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(example = OpenApiExamples.STATISTIC)
public class ShopUnitStatisticUnit extends ShopUnit {

    @JsonIgnore
    private Set<ShopUnit> children;
}
