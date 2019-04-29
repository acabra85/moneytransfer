package com.acabra.moneytransfer.response;

public class MessageResponse<T> extends SimpleResponse {

    private static final long serialVersionUID = -6406713160086373312L;
    private String message;
    private T body;

    public MessageResponse(long id, final boolean isFailure, final String message, T body) {
        super(id, isFailure);
        this.message = message;
        this.body = body;
    }

    public T getBody() {
        return body;
    }

    public String getMessage() {
        return message;
    }
}