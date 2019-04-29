package com.acabra.moneytransfer.dto;

import java.math.BigDecimal;

public class TransferRequestDTO {
    public long sourceAccountId;
    public long destinationAccountId;
    public BigDecimal amount;
}
