package ru.yandex.practicum.mymarket.business.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot create order from empty cart");
    }
}
