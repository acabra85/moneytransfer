package com.acabra.moneytransfer.dto;

import com.acabra.moneytransfer.model.Account;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {

    private final long id;
    private final BigDecimal balance;

    @JsonCreator
    public AccountDTO(@JsonProperty(value = "id", required = true) long id,
                      @JsonProperty(value = "balance", required = true)  BigDecimal balance) {
        this.id = id;
        this.balance = balance;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountDTO that = (AccountDTO) o;
        return id == that.id &&
                Objects.equals(balance, that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance);
    }
}
