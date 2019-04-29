package com.acabra.moneytransfer.dto;

import java.math.BigDecimal;

public class CreateAccountRequestDTO {

    private final BigDecimal initialBalance;

    public CreateAccountRequestDTO(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

}
