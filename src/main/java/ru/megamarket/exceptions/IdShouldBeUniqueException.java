package ru.megamarket.exceptions;

public class IdShouldBeUniqueException extends CustomValidationException {

    public IdShouldBeUniqueException() {
        super("Shop units ids should be unique");
    }
}
