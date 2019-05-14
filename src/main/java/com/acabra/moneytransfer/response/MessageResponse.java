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
    public MessageResponse(@JsonProperty(value = "id", required = true) long id,
                           @JsonProperty(value = "statusCode", required = true) int statusCode,
                           @JsonProperty(value = "isFailure", required = true) final boolean isFailure,
                           @JsonProperty(value = "message", required = true) final String message,
                           @JsonProperty(value = "body", required = true) T body) {
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