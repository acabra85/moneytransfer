package com.acabra.moneystransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.exception.InsufficientFundsException;
import com.acabra.moneytransfer.exception.InvalidDestinationAccountException;
import com.acabra.moneytransfer.exception.InvalidOperationException;
import com.acabra.moneytransfer.exception.InvalidTransferAmountException;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import com.acabra.moneytransfer.service.TransferServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class TransferServiceImplTest {

    @Mock
    AccountDAO accountDAOMock;

    @Mock
    TransferDAO transferDAOMock;

    @Mock
    AccountsTransferLock accountsTransferLockMock;

    @InjectMocks
    private TransferServiceImpl underTest;

    private LocalDateTime NOW = LocalDateTime.now();

    @Test
    public void should_transfer_amount_between_accounts() {
        //given
        Account sourceAccount = new Account(1L, new BigDecimal("200"));
        Account destinationAccount = new Account(2L, new BigDecimal("50"));
        BigDecimal transferAmount = new BigDecimal("100");
        TransferRequest transferRequest = new TransferRequest(sourceAccount.getId(), destinationAccount.getId(), transferAmount);

        List<Transfer> transfers = Collections.singletonList(new Transfer(1L, NOW, sourceAccount.getId(), destinationAccount.getId(), transferAmount));

        Mockito.when(accountDAOMock.lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId()))
                .thenReturn(accountsTransferLockMock);
        Mockito.when(accountsTransferLockMock.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountsTransferLockMock.getDestinationAccount()).thenReturn(destinationAccount);
        Mockito.when(transferDAOMock.storeTransferAndCommitTransactional(any(), any())).thenReturn(transfers.get(0));

        //when
        Transfer transfer = underTest.transfer(transferRequest);

        //then
        Assert.assertEquals(transfer.id, transfers.get(0).id);
        Assert.assertEquals(0, sourceAccount.getBalance().compareTo(new BigDecimal("100")));
        Assert.assertEquals(0, destinationAccount.getBalance().compareTo(new BigDecimal("150")));

        //verify
        Mockito.verify(accountDAOMock, Mockito.times(1)).lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId());
        Mockito.verify(accountsTransferLockMock, Mockito.times(1)).getDestinationAccount();
        Mockito.verify(accountsTransferLockMock, Mockito.times(1)).getSourceAccount();
        Mockito.verify(transferDAOMock, Mockito.times(1)).storeTransferAndCommitTransactional(any(), any());
    }

    @Test(expected = InsufficientFundsException.class)
    public void should_fail_transfer_insufficient_balance_source_account() {
        //given
        Account sourceAccount = new Account(1L, new BigDecimal("10"));
        Account destinationAccount = new Account(2L, new BigDecimal("50"));
        BigDecimal transferAmount = BigDecimal.valueOf(20L);
        TransferRequest transferRequest = new TransferRequest(sourceAccount.getId(), destinationAccount.getId(), transferAmount);

        Mockito.when(accountDAOMock.lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId())).thenReturn(accountsTransferLockMock);
        Mockito.when(accountsTransferLockMock.getSourceAccount()).thenReturn(sourceAccount);

        //when
        underTest.transfer(transferRequest);
    }

    @Test(expected = InvalidTransferAmountException.class)
    public void should_fail_transfer_amount_zero() {
        //given
        Account sourceAccount = new Account(1L, new BigDecimal("200"));
        Account destinationAccount = new Account(2L, new BigDecimal("50"));
        BigDecimal transferAmount = BigDecimal.ZERO;
        TransferRequest transferRequest = new TransferRequest(sourceAccount.getId(), destinationAccount.getId(), transferAmount);

        //when
        underTest.transfer(transferRequest);
    }

    @Test(expected = InvalidDestinationAccountException.class)
    public void should_fail_source_and_destination_account_are_the_same() {
        //given
        TransferRequest transferRequestMock = Mockito.mock(TransferRequest.class);

        Mockito.when(transferRequestMock.getSourceAccountId()).thenReturn(1L);
        Mockito.when(transferRequestMock.getDestinationAccountId()).thenReturn(1L);
        Mockito.when(transferRequestMock.getTransferAmount()).thenReturn(BigDecimal.ONE);

        //when
        underTest.transfer(transferRequestMock);
    }

    @Test(expected = NoSuchElementException.class)
    public void should_fail_non_existent_accounts() {
        //given
        Mockito.when(accountDAOMock.lockAccountsForTransfer(1L, 2L)).thenThrow(NoSuchElementException.class);

        //when
        underTest.transfer(new TransferRequest(1L, 2L, BigDecimal.TEN));

        //then
    }
}
