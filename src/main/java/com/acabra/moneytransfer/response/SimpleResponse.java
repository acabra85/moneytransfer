package com.acabra.moneytransfer.response;

import java.io.Serializable;

public abstract class SimpleResponse implements Serializable {

    protected final long id;
    protected final boolean isFailure;

    protected SimpleResponse(long id, boolean isFailure){
        this.id = id;
        this.isFailure = isFailure;
    }

    public long getId() {
        return id;
    }

    public boolean isFailure() {
        return isFailure;
    }
}
