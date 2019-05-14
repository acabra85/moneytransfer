package com.acabra.moneytransfer.exception;

public class InvalidDestinationAccountException extends RuntimeException {
    public InvalidDestinationAccountException(String message) {
        super(message);
    }
}
