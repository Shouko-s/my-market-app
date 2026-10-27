package ru.yandex.practicum.mymarket.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.mymarket.business.exception.ItemNotFoundException;

import java.io.IOException;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public void handleItemNotFound(ItemNotFoundException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }
}
