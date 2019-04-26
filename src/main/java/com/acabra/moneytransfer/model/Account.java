package com.acabra.moneytransfer.model;

import com.acabra.moneytransfer.exception.InvalidOperationException;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

public class Account {

    private final long id;
    private BigDecimal balance;

    public Account(long id, BigDecimal initialBalance) {
        this.id = id;
        this.balance = invalidDepositAmount(initialBalance) ? ZERO : initialBalance;
    }

    public Account(long id) {
        this.id = id;
        this.balance = ZERO;
    }

    synchronized public void withdraw(BigDecimal amount) {
        if (isInvalidWithdrawAmount(amount)) {
            throw new InvalidOperationException(String.format("Unable to withdraw %s from the account remaining balance %s", amount.toString(), balance.toString()));
        }
        balance = balance.subtract(amount);
    }

    synchronized public void deposit(BigDecimal amount) {
        if (invalidDepositAmount(amount)) {
            throw new InvalidOperationException(String.format("The given deposit %s must be greater than zero", amount.toString()));
        }
        balance = balance.add(amount);
    }

    private boolean isInvalidWithdrawAmount(BigDecimal amount) {
        return amount.compareTo(balance) > 0;
    }

    private boolean invalidDepositAmount(BigDecimal amount) {
        return ZERO.compareTo(amount) >= 0;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public long getId() {
        return id;
    }
}
