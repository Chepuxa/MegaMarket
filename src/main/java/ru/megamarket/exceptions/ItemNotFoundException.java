package ru.megamarket.exceptions;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException() {
        super("Shop unit not found");
    }
}
