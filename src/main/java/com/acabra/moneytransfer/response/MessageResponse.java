package com.acabra.moneytransfer.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "statusCode", "isFailure", "message", "body"})
public class MessageResponse<T> extends SimpleResponse {

    private static final long serialVersionUID = -6406713160086373312L;
    private final int statusCode;
    private String message;
    private T body;

    @JsonCreator
    public MessageResponse(@JsonProperty("id") long id,
                           @JsonProperty("statusCode") int statusCode,
                           @JsonProperty("isFailure") final boolean isFailure,
                           @JsonProperty("message") final String message,
                           @JsonProperty("body") T body) {
        super(id, isFailure);
        this.message = message;
        this.statusCode = statusCode;
        this.body = body;
    }

    @JsonProperty("body")
    public T getBody() {
        return body;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("statusCode")
    public int getStatusCode() {
        return statusCode;
    }
}