package com.acabra.moneytransfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class TransferDTO {
    public final long id;
    public final LocalDateTime timestamp;
    public final long sourceAccountId;
    public final long destinationAccountId;
    public final BigDecimal amount;

    public TransferDTO(long id, LocalDateTime timestamp, long sourceAccountId, long destinationAccountId, BigDecimal amount) {
        this.timestamp = timestamp;
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferDTO that = (TransferDTO) o;
        return id == that.id &&
                sourceAccountId == that.sourceAccountId &&
                destinationAccountId == that.destinationAccountId &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, sourceAccountId, destinationAccountId, amount);
    }
}
