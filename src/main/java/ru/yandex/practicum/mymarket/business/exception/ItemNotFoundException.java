package ru.yandex.practicum.mymarket.business.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(long id) {
        super("Item with id " + id + " not found");
    }
}
