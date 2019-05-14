package com.acabra.moneytransfer.control;

import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.acabra.moneytransfer.dto.TransferDTO;
import com.acabra.moneytransfer.dto.TransferRequestDTO;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import com.acabra.moneytransfer.response.MessageResponse;
import com.acabra.moneytransfer.service.AccountService;
import com.acabra.moneytransfer.service.TransferService;
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

    private final AccountService accountService;
    private final TransferService transferService;
    private final JsonHelper jsonHelper = JsonHelper.getInstance();
    private final AtomicLong callCounter = new AtomicLong();

    private static Logger log = LoggerFactory.getLogger(Controller.class);

    public Controller(AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    public MessageResponse<AccountDTO> createAccount(Request request, Response response) {
        try {
            CreateAccountRequestDTO createAccountRequestDTO = jsonHelper.fromJson(request.body(), CreateAccountRequestDTO.class);
            AccountDTO accountDTO = accountService.createAccount(createAccountRequestDTO);
            response.status(HttpStatus.CREATED_201);
            return new MessageResponse<>(getCallId(), HttpStatus.CREATED_201, false, "Success: Account Created", accountDTO);
        } catch (IOException e) {
            MessageResponse<AccountDTO> badRequestResponse = badRequestResponse(response, "Failed to create the account {malformed json object}: " + request.body());
            log.error(e.getMessage() + " " + badRequestResponse.getMessage(), e);
            return badRequestResponse;
        }
    }

    public MessageResponse<AccountDTO> getAccountById(Request request, Response response) {
        Long accountId = asLong(request.params(":accountId"));
        if (null == accountId) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return new MessageResponse<>(getCallId(), HttpStatus.BAD_REQUEST_400, true, "Could not find account with the given invalid id: " + request.params(":accountId"), null);
        }
        AccountDTO account = accountService.retrieveAccountById(accountId);
        if (account == null) {
            response.status(HttpStatus.NOT_FOUND_404);
            MessageResponse<AccountDTO> objectMessageResponse = new MessageResponse<>(getCallId(), HttpStatus.NOT_FOUND_404 , false, "Could not find account with the given id: " + request.params(":accountId"), null);
            log.info(objectMessageResponse.getMessage());
            return objectMessageResponse;
        }
        return new MessageResponse<>(getCallId(), HttpStatus.OK_200, false, "Success", account);
    }

    public MessageResponse<List<AccountDTO>> getAccounts(Request request, Response response) {
        return new MessageResponse<>(getCallId(), HttpStatus.OK_200, false, "Success", accountService.retrieveAccounts());
    }

    public MessageResponse<TransferDTO> transfer(Request request, Response response) {
        TransferRequestDTO transferRequestDTO;
        try {
            transferRequestDTO = jsonHelper.fromJson(request.body(), TransferRequestDTO.class);
        } catch (IOException ioe) {
            String extendedMessage = "Failed to fulfill the transfer {malformed json object}: " + request.body();
            MessageResponse<TransferDTO> badRequestResponse = badRequestResponse(response, extendedMessage);
            log.error(ioe.getMessage() + " " + badRequestResponse.getMessage(), ioe);
            return badRequestResponse;
        }
        TransferRequest transferRequest = TransferRequest.fromDTO(transferRequestDTO);
        Transfer transfer = transferService.transfer(transferRequest);
        if (null == transfer) {
            response.status(HttpStatus.BAD_REQUEST_400);
            MessageResponse<TransferDTO> failTransferResponse = new MessageResponse<>(getCallId(), HttpStatus.BAD_REQUEST_400, true, "Failed to fulfill the transfer: " + transferRequest, null);
            log.info(failTransferResponse.getMessage());
            return failTransferResponse;
        }
        response.status(HttpStatus.CREATED_201);
        return new MessageResponse<>(getCallId(), HttpStatus.CREATED_201, false, "Success", TransferDTO.fromTransfer(transfer));
    }

    public MessageResponse<List<TransferDTO>> getTransfers(Request request, Response response) {
        List<TransferDTO> collect = transferService.retrieveAllTransfers()
                .stream()
                .map(TransferDTO::fromTransfer)
                .collect(Collectors.toList());
        return new MessageResponse<>(getCallId(), HttpStatus.OK_200, false, "Success", collect);
    }

    public MessageResponse<List<TransferDTO>> retrieveTransfersByAccountId(Request request, Response response) {
        Long accountId = asLong(request.params(":accountId"));
        if (accountId == null) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return new MessageResponse<>(getCallId(), HttpStatus.BAD_REQUEST_400, true, "Could not find account with the given invalid id: " + request.params(":accountId"), null);
        }
        List<TransferDTO> transfers = transferService.retrieveAllTransfersByAccountId(accountId).stream()
                .map(TransferDTO::fromTransfer)
                .collect(Collectors.toList());
        return new MessageResponse<>(getCallId(), HttpStatus.OK_200, false, "Success", transfers);
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

    private <T> MessageResponse<T> badRequestResponse(Response response, String extendedMessage) {
        response.status(HttpStatus.BAD_REQUEST_400);
        return new MessageResponse<>(getCallId(), HttpStatus.BAD_REQUEST_400, true, extendedMessage, null);
    }
}
