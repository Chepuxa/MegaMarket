package ru.megamarket.exceptions;

public class ShopUnitTypeNotUpdatableException  extends CustomValidationException {

    public ShopUnitTypeNotUpdatableException() {
        super("Type of shop unit cannot be updated");
    }
}
