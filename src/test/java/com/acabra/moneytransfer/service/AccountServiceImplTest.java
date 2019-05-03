package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Currency;
import com.acabra.moneytransfer.utils.TestUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static java.util.Collections.EMPTY_LIST;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

    @Mock
    AccountDAO accountDAOMock;

    @InjectMocks
    AccountServiceImpl underTest;

    @Test
    public void should_retrieve_empty_list_no_accounts() {
        //given
        Mockito.when(accountDAOMock.retrieveAllAccounts()).thenReturn(EMPTY_LIST);

        //then when
        Assert.assertTrue(underTest.retrieveAccounts().isEmpty());
    }

    @Test
    public void should_retrieve_accounts() {
        //given
        ArrayList<Account> expectedAccountList = new ArrayList<Account>() {{
            add(new Account(1L, Currency.EUR));
            add(new Account(2L, Currency.EUR));
            add(new Account(3L, Currency.EUR));
            add(new Account(4L, Currency.EUR));
            add(new Account(5L, Currency.EUR));
        }};
        Mockito.when(accountDAOMock.retrieveAllAccounts()).thenReturn(expectedAccountList);

        //then when
        Assert.assertEquals(expectedAccountList.size(), underTest.retrieveAccounts().size());
    }

    @Test
    public void should_retrieve_account_by_id() {
        //given
        long accountId = 1L;
        Account account = new Account(accountId, Currency.EUR);

        Mockito.when(accountDAOMock.retrieveAccountById(accountId)).thenReturn(account);

        //then when
        AccountDTO accountDTO = underTest.retrieveAccountById(accountId);

        Assert.assertEquals(accountId, accountDTO.getId());
        TestUtils.assertBigDecimalEquals("0", accountDTO.getBalance());
    }

    @Test
    public void should_retrieve_null_for_null_account_id() {
        //given
        Long accountId = null;

        //then when
        Assert.assertNull(underTest.retrieveAccountById(accountId));
    }

    @Test
    public void should_create_account_zero_balance_for_null_balance() {
        //given
        long expectedAccountId = 1L;
        String expectedCurrencyCode = Currency.EUR.code;
        Currency expectedCurrency = Currency.EUR;
        Account expectedAccount = new Account(expectedAccountId, BigDecimal.ZERO, expectedCurrency);
        Mockito.when(accountDAOMock.createAccount(Mockito.any(), Mockito.any())).thenReturn(expectedAccount);
        CreateAccountRequestDTO createAccountRequestDTO = new CreateAccountRequestDTO(null, expectedCurrencyCode);

        //when
        AccountDTO accountDTO = underTest.createAccount(createAccountRequestDTO);

        Assert.assertEquals(expectedAccountId, accountDTO.getId());
        Assert.assertEquals(expectedCurrency.code, accountDTO.getCurrencyCode());
        TestUtils.assertBigDecimalEquals("0", accountDTO.getBalance());

        Mockito.verify(accountDAOMock,Mockito.times(1)).createAccount(Mockito.any(), Mockito.any());
    }

    @Test
    public void should_create_account_with_given_balance() {
        //given

        //when
        Assert.assertNull(underTest.retrieveAccountById(null));
    }
}
