package com.acabra.moneytransfer.control;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.acabra.moneytransfer.dto.TransferDTO;
import com.acabra.moneytransfer.dto.TransferRequestDTO;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import com.acabra.moneytransfer.response.MessageResponse;
import com.acabra.moneytransfer.service.AccountService;
import com.acabra.moneytransfer.service.AccountServiceImpl;
import com.acabra.moneytransfer.service.TransferService;
import com.acabra.moneytransfer.service.TransferServiceImpl;
import com.acabra.moneytransfer.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class Controller {

    private final TransferService transferService;
    private final AccountService accountService;
    private final JsonHelper jsonHelper;
    private AtomicLong callCounter = new AtomicLong();

    private static Logger log = LoggerFactory.getLogger(Controller.class);

    public Controller(AccountDAO accountDAO, TransferDAO transferDAO, JsonHelper jsonHelper) {
        this.accountService = new AccountServiceImpl(accountDAO);
        this.jsonHelper = jsonHelper;
        this.transferService = new TransferServiceImpl(transferDAO, accountDAO);
    }

    public MessageResponse createAccount(Request request, Response response) {
        try {
            CreateAccountRequestDTO createAccountRequestDTO = jsonHelper.fromJson(request.body(), CreateAccountRequestDTO.class);
            AccountDTO accountDTO = AccountDTO.fromAccount(accountService.createAccount(createAccountRequestDTO.getInitialBalance()));
            response.status(HttpStatus.CREATED_201);
            return new MessageResponse<>(getCallId(), false, "Success: Account Created", accountDTO);
        } catch (IOException e) {
            MessageResponse badRequestResponse = badRequestResponse(response, "Failed to create the account {malformed json object}: " + request.body());
            log.error(e.getMessage() + " " + badRequestResponse.getMessage(), e);
            return badRequestResponse;
        }
    }

    public MessageResponse getAccountById(Request request, Response response) {
        Long accountId = asLong(request.params(":accountId"));
        if (null == accountId) {
            response.status(HttpStatus.NOT_FOUND_404);
            return new MessageResponse<>(getCallId(), true, "Could not find account with the given id: " + request.params(":accountId"), null);
        }
        Account account = accountService.retrieveAccountById(accountId);
        if (account == null) {
            response.status(HttpStatus.NOT_FOUND_404);
            MessageResponse objectMessageResponse = new MessageResponse<>(getCallId(), false, "Could not find account with the given id: " + request.params(":accountId"), null);
            log.info(objectMessageResponse.getMessage());
            return objectMessageResponse;
        }
        return new MessageResponse<>(getCallId(), false, "Success", AccountDTO.fromAccount(account));
    }

    public MessageResponse getAccounts(Request request, Response response) {
        List<AccountDTO> collect = accountService.retrieveAccounts()
                .stream().map(AccountDTO::fromAccount)
                .collect(Collectors.toList());
        return new MessageResponse<>(getCallId(), false, "Success", collect);
    }

    public MessageResponse transfer(Request request, Response response) {
        TransferRequestDTO transferRequestDTO;
        try {
            transferRequestDTO = jsonHelper.fromJson(request.body(), TransferRequestDTO.class);
        } catch (IOException ioe) {
            String extendedMessage = "Failed to fulfill the transfer {malformed json object}: " + request.body();
            MessageResponse badRequestResponse = badRequestResponse(response, extendedMessage);
            log.error(ioe.getMessage() + " " + badRequestResponse.getMessage(), ioe);
            return badRequestResponse;
        }
        TransferRequest transferRequest = TransferRequest.fromDTO(transferRequestDTO);
        response.status(HttpStatus.CREATED_201);
        Transfer transfer = transferService.transfer(transferRequest);
        if (null == transfer) {
            response.status(HttpStatus.BAD_REQUEST_400);
            MessageResponse<Object> failTransferResponse = new MessageResponse<>(getCallId(), true, "Failed to fulfill the transfer: " + transferRequest, null);
            log.info(failTransferResponse.getMessage());
            return failTransferResponse;
        }
        return new MessageResponse<>(getCallId(), false, "Success", TransferDTO.fromTransfer(transfer));
    }

    public MessageResponse getTransfers(Request request, Response response) {
        List<TransferDTO> collect = transferService.retrieveAllTransfers()
                .stream()
                .map(TransferDTO::fromTransfer)
                .collect(Collectors.toList());
        return new MessageResponse<>(getCallId(), false, "Success", collect);
    }

    public MessageResponse retrieveTransfersByAccountId(Request request, Response response) {
        Long accountId = asLong(request.params(":accountId"));
        if (accountId == null) {
            response.status(HttpStatus.NOT_FOUND_404);
            return new MessageResponse<>(getCallId(), true, "Could not find account with the given id: " + request.params(":accountId"), null);
        }
        List<TransferDTO> transfers = transferService.retrieveAllTransfersByAccountId(accountId).stream()
                .map(TransferDTO::fromTransfer)
                .collect(Collectors.toList());
        return new MessageResponse<>(getCallId(), false, "Success", transfers);
    }

    public long getCallId() {
        return this.callCounter.getAndIncrement();
    }

    private Long asLong(String asString) {
        try {
            return Long.parseLong(asString);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private MessageResponse badRequestResponse(Response response, String extendedMessage) {
        response.status(HttpStatus.BAD_REQUEST_400);
        MessageResponse<Object> failTransferResponse = new MessageResponse<>(getCallId(), true, extendedMessage, null);
        return failTransferResponse;
    }
}
