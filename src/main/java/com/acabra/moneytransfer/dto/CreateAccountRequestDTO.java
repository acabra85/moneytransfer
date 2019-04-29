package com.acabra.moneytransfer.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccountRequestDTO {

    private final BigDecimal initialBalance;

    @JsonCreator
    public CreateAccountRequestDTO(@JsonProperty("initialBalance") BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

}
