package ru.megamarket.exceptions;

import ru.megamarket.dto.Error;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ValidationException;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        return ResponseEntity
                .badRequest()
                .body(Error.builder()
                        .message("Could not parse JSON")
                        .code((long) status.value())
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity
                .badRequest()
                .body(Error.builder()
                        .message("Validation of one of the fields failed")
                        .code((long) status.value())
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity
                .badRequest()
                .body(Error.builder()
                        .message("Type mismatch")
                        .code((long) status.value())
                        .build());
    }

    @ResponseBody
    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<Error> itemNotFoundHandler(ItemNotFoundException ex) {
        return new ResponseEntity<>(Error.builder()
                .code((long) HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build(), HttpStatus.NOT_FOUND);
    }

    @ResponseBody
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<Error> validationFailed(ValidationException ex) {
        return new ResponseEntity<>(Error.builder()
                .code((long) HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}
