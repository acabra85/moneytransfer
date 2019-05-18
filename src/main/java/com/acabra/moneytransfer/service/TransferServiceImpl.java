package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.exception.InsufficientFundsException;
import com.acabra.moneytransfer.exception.InvalidDestinationAccountException;
import com.acabra.moneytransfer.exception.InvalidTransferAmountException;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferServiceImpl implements TransferService {

    private final TransferDAO transferDAO;
    private final AccountDAO accountDAO;

    Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    public TransferServiceImpl(TransferDAO transferDAO, AccountDAO accountDAO) {
        this.transferDAO = transferDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public Transfer transfer(TransferRequest transferRequest) {
        validateTransferRequest(transferRequest);
        AccountsTransferLock transferLock = accountDAO.lockAccountsForTransfer(transferRequest.getSourceAccountId(), transferRequest.getDestinationAccountId());
        try {
            Account source = transferLock.getSourceAccount();
            validateSourceAccountFundsForTransfer(source, transferRequest.getTransferAmount());
            Account destination = transferLock.getDestinationAccount();
            if(source.lock.tryLock()) {
                try {
                    if (destination.lock.tryLock()) {
                        try {
                            source.withdraw(transferRequest.getTransferAmount());
                            accountDAO.updateAccountBalanceTransactional(source, transferLock.getTx());

                            destination.deposit(transferRequest.getTransferAmount());
                            accountDAO.updateAccountBalanceTransactional(destination, transferLock.getTx());

                            return transferDAO.storeTransferAndCommitTransactional(transferRequest, transferLock.getTx());
                        } finally {
                            destination.lock.unlock();
                        }
                    }
                } finally {
                    source.lock.unlock();
                }
            }
            return null;
        } catch (Exception e) {
            transferLock.rollback();
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            transferLock.close();
        }
    }

    @Override
    public List<Transfer> retrieveAllTransfers() {
        return transferDAO.retrieveAllTransfers();
    }

    @Override
    public List<Transfer> retrieveAllTransfersByAccountId(Long accountId) {
        if (null == accountId) {
            return Collections.emptyList();
        }
        return transferDAO.retrieveTransfersByAccountId(accountId);
    }

    private void validateTransferRequest(TransferRequest transferRequest) {
        if (null == transferRequest) {
            throw new NullPointerException("Invalid transfer request is null");
        }
        if (BigDecimal.ZERO.compareTo(transferRequest.getTransferAmount()) >= 0 ) {
            throw new InvalidTransferAmountException("Transfer amount should be greater than zero:" + transferRequest.getTransferAmount());
        }
        if(transferRequest.getSourceAccountId() == transferRequest.getDestinationAccountId()) {
            throw new InvalidDestinationAccountException("Destination account the same as source account: " + transferRequest.getSourceAccountId());
        }
    }

    private void validateSourceAccountFundsForTransfer(Account account, BigDecimal transferAmount) {
        if(account.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Source account [%d] has insufficient funds [%s] to transfer :" + transferAmount,
                            account.getId(), account.getBalance()));
        }
    }
}
