package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.model.TransferRequest;

public interface TransferService {

    boolean transfer(TransferRequest transferRequest);
}
