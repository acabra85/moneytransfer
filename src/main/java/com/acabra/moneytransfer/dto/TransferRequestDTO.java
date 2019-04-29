package com.acabra.moneytransfer.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequestDTO {

    public final long sourceAccountId;
    public final long destinationAccountId;
    public final BigDecimal amount;

    @JsonCreator
    public TransferRequestDTO(@JsonProperty("sourceAccountId") long sourceAccountId,
                              @JsonProperty("destinationAccountId") long destinationAccountId,
                              @JsonProperty("amount") BigDecimal amount) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }
}
