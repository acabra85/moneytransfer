package com.acabra.moneytransfer.dto;

import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Currency;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {

    private final long id;
    private final BigDecimal balance;
    private final String currencyCode;

    @JsonCreator
    public AccountDTO(@JsonProperty(value = "id", required = true) long id,
                      @JsonProperty(value = "balance", required = true) BigDecimal balance,
                      @JsonProperty(value = "currencyCode", required = true) String currencyCode) {
        this.id = id;
        this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
        this.currencyCode = currencyCode;
    }

    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(account.getId(), account.getBalance(), account.getCurrency().code);
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("balance")
    public BigDecimal getBalance() {
        return balance;
    }

    @JsonProperty("currencyCode")
    public String getCurrencyCode() {
        return currencyCode;
    }
}
