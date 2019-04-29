package com.acabra.moneytransfer.dto;

import com.acabra.moneytransfer.model.Transfer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class TransferDTO {

    public final long id;
    public final LocalDateTime timestamp;
    public final long sourceAccountId;
    public final long destinationAccountId;
    public final BigDecimal amount;

    @JsonCreator
    public TransferDTO(@JsonProperty("id") long id,
                       @JsonProperty("timestamp") LocalDateTime timestamp,
                       @JsonProperty("sourceAccountId") long sourceAccountId,
                       @JsonProperty("destinationAccountId") long destinationAccountId,
                       @JsonProperty("amount") BigDecimal amount) {
        this.timestamp = timestamp;
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    public static TransferDTO fromTransfer(Transfer transfer) {
        return new TransferDTO(transfer.id, transfer.timestamp, transfer.sourceAccountId, transfer.destinationAccountId, transfer.transferAmount);
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
