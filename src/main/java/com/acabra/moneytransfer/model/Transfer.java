package com.acabra.moneytransfer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transfer {
    public final long id;
    public final LocalDateTime timestamp;
    public final long sourceAccountId;
    public final long destinationAccountId;
    public final BigDecimal transferAmount;

    public Transfer(long id, LocalDateTime timestamp, long sourceAccountId, long destinationAccountId, BigDecimal transferAmount) {
        this.id = id;
        this.timestamp = timestamp;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.transferAmount = transferAmount;
    }

    public boolean involvesAccount(long accountId) {
        return sourceAccountId == accountId || destinationAccountId == accountId;
    }
}