package com.example.demo.web;

import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.NotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обработка ошибок для обычных MVC-контроллеров (Thymeleaf).
 * Для REST-эндпоинтов используется {@link RestExceptionHandler}.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NotFoundException.class, ForbiddenException.class, OptimisticLockingFailureException.class})
    public String handleDomainExceptions(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleOtherExceptions(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Произошла неожиданная ошибка");
        return "error";
    }
}




