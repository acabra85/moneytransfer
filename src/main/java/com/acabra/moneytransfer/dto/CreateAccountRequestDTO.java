package com.acabra.moneytransfer.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccountRequestDTO {

    private final BigDecimal initialBalance;
    private final String currencyCode;

    @JsonCreator
    public CreateAccountRequestDTO(@JsonProperty(value = "initialBalance", required = true)
                                   BigDecimal initialBalance,
                                   @JsonProperty(value = "currencyCode", required = true)
                                   String currencyCode) {
        this.initialBalance = initialBalance;
        this.currencyCode = currencyCode;
    }

    @JsonProperty("initialBalance")
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    @JsonProperty("currencyCode")
    public String getCurrencyCode() {
        return currencyCode;
    }
}
