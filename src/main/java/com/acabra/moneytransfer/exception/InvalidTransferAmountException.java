package com.acabra.moneytransfer.exception;

public class InvalidTransferAmountException extends RuntimeException {
    public InvalidTransferAmountException(String message){
        super(message);
    }
}
