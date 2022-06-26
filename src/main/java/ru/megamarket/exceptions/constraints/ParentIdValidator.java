package ru.megamarket.exceptions.constraints;

import ru.megamarket.dto.ShopUnitImport;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ParentIdValidator implements ConstraintValidator<ParentIdConstraint, ShopUnitImport> {

    @Override
    public void initialize(ParentIdConstraint parentIdConstraint) {
    }

    public boolean isValid(ShopUnitImport itemDto,
                           ConstraintValidatorContext cxt) {
        return itemDto.getParentId() == null || !itemDto.getParentId().equals(itemDto.getId());
    }
}
