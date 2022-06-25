package ru.megamarket.exceptions;

public class ParentWasNotFoundException extends CustomValidationException {

    public ParentWasNotFoundException() {
        super("Parent for one of the shop units was not found");
    }
}
