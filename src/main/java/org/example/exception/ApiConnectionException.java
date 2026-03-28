package org.example.exception;

public class ApiConnectionException extends ApiException {

    public ApiConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}