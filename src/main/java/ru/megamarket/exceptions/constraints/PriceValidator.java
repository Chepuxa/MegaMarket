package ru.megamarket.exceptions.constraints;

import ru.megamarket.dto.ShopUnitImport;
import ru.megamarket.dto.ShopUnitType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PriceValidator implements ConstraintValidator<CategoryPriceConstraint, ShopUnitImport> {

    @Override
    public void initialize(CategoryPriceConstraint priceAnnotation) {
    }

    public boolean isValid(ShopUnitImport itemDto,
                           ConstraintValidatorContext cxt) {
        if (itemDto.getType() == ShopUnitType.CATEGORY) return itemDto.getPrice() == null;
        if (itemDto.getType() == ShopUnitType.OFFER) {
            if (itemDto.getPrice() != null) {
                return itemDto.getPrice() >= 0;
            } else return false;
        }
        return true;
    }
}
