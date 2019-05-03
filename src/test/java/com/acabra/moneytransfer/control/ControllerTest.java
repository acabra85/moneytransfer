package com.acabra.moneytransfer.control;

import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.acabra.moneytransfer.dto.TransferDTO;
import com.acabra.moneytransfer.dto.TransferRequestDTO;
import com.acabra.moneytransfer.exception.InsufficientFundsException;
import com.acabra.moneytransfer.exception.InvalidDestinationAccountException;
import com.acabra.moneytransfer.exception.InvalidTransferAmountException;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Currency;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.response.MessageResponse;
import com.acabra.moneytransfer.service.AccountService;
import com.acabra.moneytransfer.service.TransferService;
import com.acabra.moneytransfer.utils.JsonHelper;
import com.acabra.moneytransfer.utils.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class ControllerTest {

    @Mock
    TransferService transferService;

    @Mock
    AccountService accountServiceMock;

    @InjectMocks
    Controller underTest;

    Request req = Mockito.mock(Request.class);
    Response resp = Mockito.mock(Response.class);
    JsonHelper jsonHelper = JsonHelper.getInstance();

    LocalDateTime NOW = LocalDateTime.now(Clock.systemUTC()); //reduce the calls to the now method

    @Test
    public void should_create_account() throws JsonProcessingException {
        //given
        String currencyCode = "EUR";
        BigDecimal initialBalance = BigDecimal.TEN;
        String reqBody = jsonHelper.toJson(new CreateAccountRequestDTO(initialBalance, currencyCode));
        AccountDTO expectedAccountDTO = AccountDTO.fromAccount(new Account(0L, initialBalance, Currency.EUR));

        Mockito.when(req.body()).thenReturn(reqBody);
        Mockito.when(accountServiceMock.createAccount(Mockito.any())).thenReturn(expectedAccountDTO);

        //when
        MessageResponse<AccountDTO> accountCreateResponse = underTest.createAccount(req, resp);

        //then
        Assert.assertFalse(accountCreateResponse.isFailure());
        Assert.assertEquals(0L, accountCreateResponse.getId());
        Assert.assertEquals(expectedAccountDTO, accountCreateResponse.getBody());

        Mockito.verify(accountServiceMock, Mockito.times(1)).createAccount(Mockito.any());
    }

    @Test
    public void should_fail_account_creation_invalid_request_object() {
        //given
        String reqBody = "{}";

        Mockito.when(req.body()).thenReturn(reqBody);

        //when
        MessageResponse accountCreateResponse = underTest.createAccount(req, resp);

        //then
        Assert.assertTrue(accountCreateResponse.isFailure());
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, accountCreateResponse.getStatusCode());
    }


    @Test
    public void should_return_not_found_invalid_id() {
        //given
        String invalidId = "m";
        Mockito.when(req.params(":accountId")).thenReturn(invalidId);

        //when
        MessageResponse accountCreateResponse = underTest.getAccountById(req, resp);

        //then
        Assert.assertTrue(accountCreateResponse.isFailure());
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, accountCreateResponse.getStatusCode());
    }

    @Test
    public void should_return_not_found_non_existent_account() {
        //given
        String nonExistentAccountId = "10";

        Mockito.when(req.params(":accountId")).thenReturn(nonExistentAccountId);

        //when
        MessageResponse<AccountDTO> accountCreateResponse = underTest.getAccountById(req, resp);

        //then
        Assert.assertFalse(accountCreateResponse.isFailure());
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, accountCreateResponse.getStatusCode());
    }

    @Test
    public void should_return_account_for_given_id() {
        //given
        AccountDTO existentAccount = AccountDTO.fromAccount(new Account(1L, BigDecimal.TEN, Currency.EUR));
        String existentAccountIdParam = Long.toString(existentAccount.getId());

        Mockito.when(req.params(":accountId")).thenReturn(existentAccountIdParam);
        Mockito.when(accountServiceMock.retrieveAccountById(existentAccount.getId())).thenReturn(existentAccount);

        //when
        MessageResponse<AccountDTO> accountCreateResponse = underTest.getAccountById(req, resp);

        //then
        Assert.assertFalse(accountCreateResponse.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, accountCreateResponse.getStatusCode());
    }

    @Test
    public void should_return_empty_list_no_accounts_created() {
        //given
        Mockito.when(accountServiceMock.retrieveAccounts()).thenReturn(Collections.emptyList());

        //when
        MessageResponse<List<AccountDTO>> accountsResponse = underTest.getAccounts(req, resp);

        //then
        Assert.assertFalse(accountsResponse.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, accountsResponse.getStatusCode());
        Assert.assertTrue(accountsResponse.getBody().isEmpty());

    }

    @Test
    public void should_return_a_list_with_all_created_accounts() {
        //given
        List<Account> createdAccounts = new ArrayList<Account>(){{
            add(new Account(1L, BigDecimal.TEN, Currency.EUR));
            add(new Account(2L, BigDecimal.ZERO, Currency.EUR));
            add(new Account(3L, BigDecimal.ZERO, Currency.EUR));
        }};

        Mockito.when(accountServiceMock.retrieveAccounts()).thenReturn(createdAccounts.stream().map(AccountDTO::fromAccount).collect(Collectors.toList()));

        //when
        MessageResponse<List<AccountDTO>> accountsResponse = underTest.getAccounts(req, resp);

        //then
        for (int i = 0; i < createdAccounts.size(); i++) {
            Assert.assertEquals(createdAccounts.get(i).getId(), accountsResponse.getBody().get(i).getId());
        }
        Assert.assertFalse(accountsResponse.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, accountsResponse.getStatusCode());
        Assert.assertEquals(createdAccounts.size(), accountsResponse.getBody().size());
    }

    @Test
    public void should_return_empty_list_no_transfers() {
        //given
        Mockito.when(transferService.retrieveAllTransfers()).thenReturn(Collections.emptyList());

        //when
        MessageResponse<List<TransferDTO>> transfersResponse = underTest.getTransfers(req, resp);

        //then
        Assert.assertFalse(transfersResponse.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, transfersResponse.getStatusCode());
        Assert.assertTrue(transfersResponse.getBody().isEmpty());
    }

    @Test
    public void should_fail_retrieving_transfers_invalid_given_account_id() {
        //given
        String invalidAccountID = "mk";
        Mockito.when(req.params(":accountId")).thenReturn(invalidAccountID);

        //when
        MessageResponse<List<TransferDTO>> transfersResponse = underTest.retrieveTransfersByAccountId(req, resp);

        //then
        Assert.assertTrue(transfersResponse.isFailure());
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, transfersResponse.getStatusCode());
        Assert.assertNull(transfersResponse.getBody());

        Mockito.verify(req, Mockito.times(2)).params(":accountId");
    }

    @Test
    public void should_return_empty_list_no_transfers_for_given_valid_account() {
        //given
        String accountWithExistentTransfersId = "1";
        Mockito.when(req.params(":accountId")).thenReturn(accountWithExistentTransfersId);
        Mockito.when(transferService.retrieveAllTransfersByAccountId(Mockito.anyLong())).thenReturn(Collections.emptyList());

        //when
        MessageResponse<List<TransferDTO>> transfersResponse = underTest.retrieveTransfersByAccountId(req, resp);

        //then
        Assert.assertFalse(transfersResponse.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, transfersResponse.getStatusCode());
        Assert.assertTrue(transfersResponse.getBody().isEmpty());
    }

    @Test
    public void should_return_list_of_transfers_for_given_account() {
        //given
        String accountWithExistentTransfersId = "1";
        Mockito.when(req.params(":accountId")).thenReturn(accountWithExistentTransfersId);
        List<Transfer> transfers = new ArrayList<Transfer>() {{
            add(new Transfer(1L, NOW, 1L, 2L, BigDecimal.ONE));
            add(new Transfer(1L, NOW, 1L, 2L, BigDecimal.ONE));
        }};
        Mockito.when(transferService.retrieveAllTransfersByAccountId(Mockito.anyLong())).thenReturn(transfers);

        //when
        MessageResponse<List<TransferDTO>> transfersResponse = underTest.retrieveTransfersByAccountId(req, resp);

        //then
        Assert.assertFalse(transfersResponse.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, transfersResponse.getStatusCode());
        Assert.assertEquals(transfers.size(), transfersResponse.getBody().size());
    }

    @Test
    public void should_transfer_funds_between_accounts() throws JsonProcessingException {
        //given
        long sourceAccountId = 1L;
        long destinationAccountId = 2L;
        BigDecimal transferAmount = BigDecimal.ONE;
        TransferRequestDTO transferRequestDTO = new TransferRequestDTO(sourceAccountId,
                destinationAccountId, transferAmount);
        Transfer expectedTransfer = new Transfer(1L, NOW, transferRequestDTO.getSourceAccountId(),
                transferRequestDTO.getDestinationAccountId(), transferRequestDTO.getAmount());

        Mockito.when(req.body()).thenReturn(jsonHelper.toJson(transferRequestDTO));
        Mockito.when(transferService.transfer(Mockito.any())).thenReturn(expectedTransfer);

        //when
        MessageResponse<TransferDTO> transferResponse = underTest.transfer(req, resp);
        TransferDTO receivedTransfer = transferResponse.getBody();

        //then
        Assert.assertFalse(transferResponse.isFailure());
        Assert.assertEquals(HttpStatus.CREATED_201, transferResponse.getStatusCode());
        Assert.assertEquals(sourceAccountId, receivedTransfer.getSourceAccountId());
        Assert.assertEquals(destinationAccountId, receivedTransfer.getDestinationAccountId());
        TestUtils.assertBigDecimalEquals(transferAmount, receivedTransfer.getAmount());
        Assert.assertNotNull(receivedTransfer.getTimestamp());

        Mockito.verify(req, Mockito.times(1)).body();
        Mockito.verify(transferService, Mockito.times(1)).transfer(Mockito.any());
    }

    @Test
    public void should_fail_transfer_funds_between_accounts_invalid_request() {
        //given
        String invalidTransferRequestDTO = "{}";

        Mockito.when(req.body()).thenReturn(invalidTransferRequestDTO);

        //when
        MessageResponse<TransferDTO> transferResponse = underTest.transfer(req, resp);

        //then
        Assert.assertTrue(transferResponse.isFailure());
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, transferResponse.getStatusCode());
        Assert.assertNull(transferResponse.getBody());

        Mockito.verify(req, Mockito.times(2)).body();
    }

    @Test(expected = InvalidTransferAmountException.class)
    public void should_fail_transfer_funds_between_accounts_invalid_transfer_amount() throws JsonProcessingException {
        //given
        TransferRequestDTO transferRequestDTO = new TransferRequestDTO(1L, 1L, BigDecimal.ZERO);

        Mockito.when(req.body()).thenReturn(jsonHelper.toJson(transferRequestDTO));

        //when
        underTest.transfer(req, resp);
    }

    @Test(expected = InvalidDestinationAccountException.class)
    public void should_fail_transfer_funds_between_accounts_invalid_destination_account() throws JsonProcessingException {
        //given
        TransferRequestDTO transferRequestDTO = new TransferRequestDTO(1L, 1L, BigDecimal.ONE);

        Mockito.when(req.body()).thenReturn(jsonHelper.toJson(transferRequestDTO));

        //when
        underTest.transfer(req, resp);
    }

    @Test(expected = InsufficientFundsException.class)
    public void should_fail_transfer_funds_between_accounts_insufficient_funds_on_source() throws JsonProcessingException {
        //given
        TransferRequestDTO transferRequestDTO = new TransferRequestDTO(1L, 2L, BigDecimal.ONE);

        Mockito.when(req.body()).thenReturn(jsonHelper.toJson(transferRequestDTO));
        Mockito.when(transferService.transfer(Mockito.any())).thenThrow(new InsufficientFundsException(""));

        //when
        underTest.transfer(req, resp);
    }

    @Test(expected = NoSuchElementException.class)
    public void should_fail_transfer_funds_between_accounts_non_existent_account() throws JsonProcessingException {
        //given
        TransferRequestDTO transferRequestDTO = new TransferRequestDTO(-1L, 2L, BigDecimal.ONE);

        Mockito.when(req.body()).thenReturn(jsonHelper.toJson(transferRequestDTO));
        Mockito.when(transferService.transfer(Mockito.any())).thenThrow(NoSuchElementException.class);

        //when
        underTest.transfer(req, resp);
    }

    @Test
    public void should_fail_transfer_funds_between_service_returned_null() throws JsonProcessingException {
        //given
        TransferRequestDTO transferRequestDTO = new TransferRequestDTO(1L, 2L, BigDecimal.ONE);

        Mockito.when(req.body()).thenReturn(jsonHelper.toJson(transferRequestDTO));
        Mockito.when(transferService.transfer(Mockito.any())).thenReturn(null);

        //when
        MessageResponse<TransferDTO> transferResponse = underTest.transfer(req, resp);
        //then

        Assert.assertTrue(transferResponse.isFailure());
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, transferResponse.getStatusCode());
        Assert.assertNull(transferResponse.getBody());

        Mockito.verify(req, Mockito.times(1)).body();
    }

}
