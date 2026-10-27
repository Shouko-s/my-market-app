package ru.yandex.practicum.mymarket.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.mymarket.business.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.business.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.business.exception.OrderNotFoundException;

import java.io.IOException;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler({ItemNotFoundException.class, OrderNotFoundException.class})
    public void handleNotFound(RuntimeException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(EmptyCartException.class)
    public String handleEmptyCart() {
        return "redirect:/cart/items";
    }
}
