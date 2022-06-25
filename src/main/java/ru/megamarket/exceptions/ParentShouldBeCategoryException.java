package ru.megamarket.exceptions;

public class ParentShouldBeCategoryException  extends CustomValidationException {
    public ParentShouldBeCategoryException() {
        super("Parent cannot be of type OFFER");
    }
}
