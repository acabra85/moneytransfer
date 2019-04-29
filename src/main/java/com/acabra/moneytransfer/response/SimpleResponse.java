package com.acabra.moneytransfer.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SimpleResponse implements Serializable {

    @JsonProperty("id")
    protected final long id;

    protected final boolean isFailure;

    @JsonCreator
    protected SimpleResponse( @JsonProperty("id") long id,  @JsonProperty("isFailure") boolean isFailure){
        this.id = id;
        this.isFailure = isFailure;
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("isFailure")
    public boolean isFailure() {
        return isFailure;
    }
}
