package com.acabra.moneytransfer.dto;

import com.acabra.moneytransfer.model.Transfer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public class TransferDTO {

    private final long id;
    private final LocalDateTime timestamp;
    private final long sourceAccountId;
    private final long destinationAccountId;
    private final BigDecimal amount;

    @JsonCreator
    public TransferDTO(@JsonProperty(value = "id", required = true) long id,
                       @JsonProperty(value = "timestamp", required = true) LocalDateTime timestamp,
                       @JsonProperty(value = "sourceAccountId", required = true) long sourceAccountId,
                       @JsonProperty(value = "destinationAccountId", required = true) long destinationAccountId,
                       @JsonProperty(value = "amount", required = true) BigDecimal amount) {
        this.timestamp = timestamp;
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static TransferDTO fromTransfer(Transfer transfer) {
        return new TransferDTO(transfer.id, transfer.timestamp, transfer.sourceAccountId, transfer.destinationAccountId, transfer.transferAmount);
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("timestamp")
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @JsonProperty("sourceAccountId")
    public long getSourceAccountId() {
        return sourceAccountId;
    }

    @JsonProperty("destinationAccountId")
    public long getDestinationAccountId() {
        return destinationAccountId;
    }

    @JsonProperty("amount")
    public BigDecimal getAmount() {
        return amount;
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
