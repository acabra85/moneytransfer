package com.acabra.moneytransfer.dto;

import com.acabra.moneytransfer.model.Account;
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

    @JsonCreator
    public AccountDTO(@JsonProperty(value = "id", required = true) long id,
                      @JsonProperty(value = "balance", required = true)  BigDecimal balance) {
        this.id = id;
        this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(account.getId(), account.getBalance());
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("balance")
    public BigDecimal getBalance() {
        return balance;
    }
}
