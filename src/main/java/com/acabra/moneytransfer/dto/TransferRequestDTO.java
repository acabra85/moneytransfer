package com.acabra.moneytransfer.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequestDTO {

    private final long sourceAccountId;
    private final long destinationAccountId;
    private final BigDecimal amount;

    @JsonCreator
    public TransferRequestDTO(@JsonProperty(value = "sourceAccountId", required = true)
                              long sourceAccountId,
                              @JsonProperty(value = "destinationAccountId", required = true)
                              long destinationAccountId,
                              @JsonProperty(value = "amount", required = true)
                              BigDecimal amount) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
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
}
