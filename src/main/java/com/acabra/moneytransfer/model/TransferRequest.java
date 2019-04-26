package com.acabra.moneytransfer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferRequest {

    public final long sourceAccountId;
    public final long destinationAccountId;
    public final BigDecimal transferAmount;
    public final LocalDateTime timestamp;

    public TransferRequest(long sourceAccountId, long destinationAccountId, BigDecimal transferAmount, LocalDateTime timestamp) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.transferAmount = transferAmount;
        this.timestamp = timestamp;
    }
}
