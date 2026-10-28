package ru.yandex.practicum.mymarket.business.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(long id) {
        super("Order with id " + id + " not found");
    }
}
