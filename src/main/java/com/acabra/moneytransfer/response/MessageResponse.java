package com.acabra.moneytransfer.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "isFailure", "message", "body"})
public class MessageResponse<T> extends SimpleResponse {

    private static final long serialVersionUID = -6406713160086373312L;
    private String message;
    private T body;

    @JsonCreator
    public MessageResponse(@JsonProperty("id") long id,
                           @JsonProperty("isFailure") final boolean isFailure,
                           @JsonProperty("message") final String message,
                           @JsonProperty("body") T body) {
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