package com.acabra.moneytransfer.request;

import com.acabra.moneytransfer.dto.TransferRequestDTO;
import com.acabra.moneytransfer.exception.InvalidDestinationAccountException;
import com.acabra.moneytransfer.exception.InvalidTransferAmountException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferRequest {

    private final long sourceAccountId;
    private final long destinationAccountId;
    private final BigDecimal transferAmount;
    private final LocalDateTime timestamp;

    public TransferRequest(long sourceAccountId, long destinationAccountId, BigDecimal transferAmount) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.transferAmount = transferAmount;
        this.timestamp = LocalDateTime.now();
    }

    public long getSourceAccountId() {
        return sourceAccountId;
    }

    public long getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public static TransferRequest fromDTO(TransferRequestDTO transferRequestDTO) {
        if (BigDecimal.ZERO.compareTo(transferRequestDTO.amount) >= 0) {
            throw new InvalidTransferAmountException("The transfer amount should be greater than zero");
        }
        if (transferRequestDTO.sourceAccountId == transferRequestDTO.destinationAccountId) {
            throw new InvalidDestinationAccountException("Destination account must be different than the source account");
        }
        return new TransferRequest(transferRequestDTO.sourceAccountId, transferRequestDTO.destinationAccountId, transferRequestDTO.amount);
    }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "sourceAccountId=" + sourceAccountId +
                ", destinationAccountId=" + destinationAccountId +
                ", transferAmount=" + transferAmount +
                '}';
    }
}
