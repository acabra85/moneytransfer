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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
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

        Transfer internalTransfer = new Transfer(1L, NOW, sourceAccount.getId(), destinationAccount.getId(), transferAmount);
        AccountsTransferLock accountTransferLock = Mockito.mock(AccountsTransferLock.class);

        Mockito.when(accountTransferLock.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountTransferLock.getDestinationAccount()).thenReturn(destinationAccount);
        Mockito.when(accountDAOMock.lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId())).thenReturn(accountTransferLock);
        Mockito.when(transferDAOMock.storeTransferAndCommitTransactional(any(), any())).thenReturn(internalTransfer);

        //when
        Transfer transfer = underTest.transfer(transferRequest);

        //then
        Assert.assertEquals(transfer.id, internalTransfer.id);
        Assert.assertEquals(0, sourceAccount.getBalance().compareTo(new BigDecimal("100")));
        Assert.assertEquals(0, destinationAccount.getBalance().compareTo(new BigDecimal("150")));

        //verify
        Mockito.verify(accountDAOMock, Mockito.times(1)).lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId());
        Mockito.verify(transferDAOMock, Mockito.times(1)).storeTransferAndCommitTransactional(any(), any());
        Mockito.verify(accountTransferLock, Mockito.times(1)).getDestinationAccount();
        Mockito.verify(accountTransferLock, Mockito.times(1)).getSourceAccount();


    }

    @Test(expected = InsufficientFundsException.class)
    public void should_fail_transfer_insufficient_balance_source_account() {
        //given
        Account sourceAccount = new Account(1L, new BigDecimal("10"));
        Account destinationAccount = new Account(2L, new BigDecimal("50"));
        BigDecimal transferAmount = BigDecimal.valueOf(20L);
        TransferRequest transferRequest = new TransferRequest(sourceAccount.getId(), destinationAccount.getId(), transferAmount);
        AccountsTransferLock accountTransferLockMock = Mockito.mock(AccountsTransferLock.class);

        Mockito.when(accountTransferLockMock.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountDAOMock.lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId())).thenReturn(accountTransferLockMock);

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
    public void should_fail_transfer_source_and_destination_account_are_the_same() {
        //given
        TransferRequest transferRequestMock = Mockito.mock(TransferRequest.class);

        Mockito.when(transferRequestMock.getSourceAccountId()).thenReturn(1L);
        Mockito.when(transferRequestMock.getDestinationAccountId()).thenReturn(1L);
        Mockito.when(transferRequestMock.getTransferAmount()).thenReturn(BigDecimal.ONE);

        //when
        underTest.transfer(transferRequestMock);
    }

    @Test(expected = NoSuchElementException.class)
    public void should_fail_transfer_non_existent_accounts() {
        //given
        Mockito.when(accountDAOMock.lockAccountsForTransfer(1L, 2L)).thenThrow(NoSuchElementException.class);

        //when
        underTest.transfer(new TransferRequest(1L, 2L, BigDecimal.TEN));

        //then
    }

    @Test
    public void should_fail_transfer_unable_to_obtain_account_lock_on_source_account() {
        //given
        Account sourceAccount = new Account(1L, BigDecimal.TEN);
        Account destinationAccount = new Account(2L, BigDecimal.TEN);
        AccountsTransferLock accountTransferLock = Mockito.mock(AccountsTransferLock.class);

        Mockito.when(accountDAOMock.lockAccountsForTransfer(1L, 2L)).thenReturn(accountTransferLock);
        Mockito.when(accountTransferLock.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(accountTransferLock.getDestinationAccount()).thenReturn(destinationAccount);

        //lock the source account on a different thread
        Executors.newFixedThreadPool(2).submit(acquireLockOnDifferentThread(sourceAccount));

        //when
        Transfer transfer = underTest.transfer(new TransferRequest(1L, 2L, BigDecimal.TEN));

        //then
        Assert.assertNull(transfer);
        Mockito.verify(accountTransferLock, Mockito.times(1)).getDestinationAccount();
        Mockito.verify(accountTransferLock, Mockito.times(1)).getSourceAccount();
    }

    @Test
    public void should_fail_transfer_unable_to_obtain_account_lock_on_destination_account() {
        //given
        Account sourceAccount = new Account(3L, BigDecimal.TEN);
        Account destinationAccount = new Account(6L, BigDecimal.TEN);
        AccountsTransferLock lock = Mockito.mock(AccountsTransferLock.class);

        Mockito.when(accountDAOMock.lockAccountsForTransfer(1L, 2L)).thenReturn(lock);
        Mockito.when(lock.getSourceAccount()).thenReturn(sourceAccount);
        Mockito.when(lock.getDestinationAccount()).thenReturn(destinationAccount);

        //lock the source account on a different thread
        Executors.newFixedThreadPool(2).submit(acquireLockOnDifferentThread(destinationAccount));

        //when
        Transfer transfer = underTest.transfer(new TransferRequest(1L, 2L, BigDecimal.TEN));

        //then
        Assert.assertNull(transfer);
        Mockito.verify(lock, Mockito.times(1)).getDestinationAccount();
        Mockito.verify(lock, Mockito.times(1)).getSourceAccount();
    }

    @Test
    public void should_return_empty_no_transfers_made() {
        //given
        Mockito.when(transferDAOMock.retrieveAllTransfers()).thenReturn(Collections.emptyList());

        //when
        List<Transfer> allTransfers = underTest.retrieveAllTransfers();

        //then
        Assert.assertTrue(allTransfers.isEmpty());
    }

    @Test
    public void should_return_available_transfers() {
        //given
        List<Transfer> availableTransfers = new ArrayList<Transfer>(){{
            add(new Transfer(1L, NOW, 1L, 2L, BigDecimal.TEN));
            add(new Transfer(2L, NOW, 1L, 2L, BigDecimal.TEN));
            add(new Transfer(3L, NOW, 1L, 2L, BigDecimal.TEN));
            add(new Transfer(4L, NOW, 1L, 2L, BigDecimal.TEN));
        }};

        Mockito.when(transferDAOMock.retrieveAllTransfers()).thenReturn(availableTransfers);

        //when
        List<Transfer> allTransfers = underTest.retrieveAllTransfers();

        //then
        Assert.assertEquals(availableTransfers.size(), allTransfers.size());
    }

    @Test
    public void should_return_available_transfers_for_given_account_id() {
        //given
        final long sourceAccountId = 1L;
        List<Transfer> availableTransfers = new ArrayList<Transfer>(){{
            add(new Transfer(0L, NOW, sourceAccountId, 2L, BigDecimal.TEN));
            add(new Transfer(2L, NOW, sourceAccountId, 2L, BigDecimal.TEN));
            add(new Transfer(3L, NOW, 2L, 2L, BigDecimal.TEN));
            add(new Transfer(4L, NOW, 3L, 2L, BigDecimal.TEN));
        }};

        Mockito.when(transferDAOMock.retrieveTransfersByAccountId(sourceAccountId))
                .thenReturn(availableTransfers.stream()
                        .filter(tfx-> tfx.involvesAccount(sourceAccountId))
                        .collect(Collectors.toList()));

        //when
        List<Transfer> allTransfersByAccount = underTest.retrieveAllTransfersByAccountId(sourceAccountId);

        //then
        Assert.assertEquals(2, allTransfersByAccount.size());
    }

    @Test
    public void should_return_empty_list_for_null_account_id() {
        //given
        final Long sourceAccountId = null;

        //when
        List<Transfer> allTransfersByAccount = underTest.retrieveAllTransfersByAccountId(sourceAccountId);

        //then
        Assert.assertTrue(allTransfersByAccount.isEmpty());
    }

    private Runnable acquireLockOnDifferentThread(Account accountToLock) {
        return () -> {
            accountToLock.lock.lock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                accountToLock.lock.unlock();
            }
        };
    }

}
