package org.example.exception;

public class ApiTimeoutException extends ApiException {

    public ApiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}