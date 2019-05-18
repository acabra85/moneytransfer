package com.acabra.moneytransfer.api;

import com.acabra.moneytransfer.control.Controller;
import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dao.h2.TransferDAOH2Impl;
import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.acabra.moneytransfer.dto.TransferDTO;
import com.acabra.moneytransfer.dto.TransferRequestDTO;
import com.acabra.moneytransfer.model.Currency;
import com.acabra.moneytransfer.response.MessageResponse;
import com.acabra.moneytransfer.service.AccountServiceImpl;
import com.acabra.moneytransfer.service.TransferServiceImpl;
import com.acabra.moneytransfer.utils.JsonHelper;
import com.acabra.moneytransfer.utils.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Sql2o;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class RouterTest {

    JsonHelper jsonHelper = JsonHelper.getInstance();
    TypeRef<MessageResponse<List<TransferDTO>>> TRANSFER_LIST_RESPONSE_TYPE_REF = new TypeRef<MessageResponse<List<TransferDTO>>>() {};
    TypeRef<MessageResponse<List<AccountDTO>>> ALL_ACCOUNTS_RESPONSE_TYPE_REF = new TypeRef<MessageResponse<List<AccountDTO>>>() {};

    //endpoints
    private static final String POST_ACCOUNT_URI = "/accounts/new";
    private static final String GET_ACCOUNT_BY_ID_URI = "/accounts/{accountId}";
    private static final String GET_ALL_ACCOUNTS = "/accounts";
    private static final String GET_ALL_ACCOUNT_TRANSFERS = "/accounts/{accountId}/transfers";
    private static final String POST_TRANSFER_URI = "/transfers/new";
    private static final String GET_ALL_TRANSFERS_URI = "/transfers";
    private static final String GET_ALL_TRANSFERS_BY_ACCOUNT_ID_URI = "/transfers/{accountId}";

    private Sql2o sql2o;

    @BeforeClass
    public static void setup() {
        RestAssured.port = 4567;
        RestAssured.basePath = "/api";
        RestAssured.baseURI = "http://localhost";
    }

    @Before
    public void before() {
        sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        new Router(new Controller(new AccountServiceImpl(accountDAO),
        new TransferServiceImpl(new TransferDAOH2Impl(sql2o), accountDAO))).registerRoutes();
    }

    @After
    public void after() {
        sql2o = null;
    }

    @Test
    public void should_create_account() {
        String initialBalance = "2000";
        Currency expectedCurrency = Currency.COP;
        shouldCreateAccount(1, initialBalance, expectedCurrency);
    }

    @Test
    public void should_create_account_negative_balance() {
        String initialBalance = "-2000";
        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(1, initialBalance, expectedCurrency);
    }

    @Test
    public void should_retrieve_created_account() {
        //given
        String initialBalance = "5000";
        int accountId = 1;
        Currency expectedCurrency = Currency.EUR;

        //when
        shouldCreateAccount(accountId, initialBalance, expectedCurrency);

        //then
        validateAccount(accountId, initialBalance, expectedCurrency);
    }

    @Test
    public void should_retrieve_empty_list_no_accounts_created(){
        given()
        .when().get(GET_ALL_ACCOUNTS)
        .then().assertThat().statusCode(HttpStatus.OK_200)
            .and().contentType(ContentType.JSON)
            .and().body("isFailure", equalTo(false))
            .and().body("body", Matchers.hasSize(0));
    }

    @Test
    public void should_retrieve_list_with_accounts_created() {
        //given
        int accountsCreated = 3;
        Currency expectedCurrency = Currency.USD;
        shouldCreateAccount(1, "110", expectedCurrency);
        shouldCreateAccount(2, "320", expectedCurrency);
        shouldCreateAccount(3, "230", expectedCurrency);

        //when
        MessageResponse<List<AccountDTO>> accountsResponse = get(GET_ALL_ACCOUNTS).as(ALL_ACCOUNTS_RESPONSE_TYPE_REF);

        //then
        Assert.assertFalse(accountsResponse.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, accountsResponse.getStatusCode());
        Assert.assertEquals(accountsCreated, accountsResponse.getBody().size());
        TestUtils.assertBigDecimalEquals("660", accountsResponse.getBody()
                .stream()
                .map(AccountDTO::getBalance)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO));
    }

    @Test
    public void should_fail_create_account_bad_request() {
        given()
            .body("{}")
        .when()
            .get(POST_ACCOUNT_URI)
        .then()
            .assertThat().contentType(ContentType.JSON)
            .and().body("isFailure", equalTo(true));
    }

    @Test
    public void should_place_transfer_same_currency() {
        //given
        int sourceAccountId = 1;
        int destinationAccountId = 2;
        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(sourceAccountId, "1800.25", expectedCurrency);
        shouldCreateAccount(destinationAccountId, "300.05", expectedCurrency);
        String transferAmount = "150.10";
        TransferRequestDTO transferRequestDTO = buildTransferRequestDTO(sourceAccountId, destinationAccountId, transferAmount);

        //when
        shouldPlaceTransfer(transferRequestDTO);

        //then
        validateAccount(sourceAccountId, "1650.15", expectedCurrency);
        validateAccount(destinationAccountId, "450.15", expectedCurrency);
    }

    @Test
    public void should_fail_place_transfer_source_account_not_found() {
        //given
        int sourceAccountId = -1;
        int destinationAccountId = 1;
        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(destinationAccountId, "300.05", expectedCurrency);
        String transferAmount = "100";
        TransferRequestDTO transferRequestDTO = buildTransferRequestDTO(sourceAccountId, destinationAccountId, transferAmount);

        //when
        shouldFailTransferBadRequest(transferRequestDTO, HttpStatus.NOT_FOUND_404);

        //then
    }

    @Test
    public void should_fail_place_transfer_destination_account_not_found() {
        //given
        int sourceAccountId = 1;
        int destinationAccountId = -1;
        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(sourceAccountId, "300.05", expectedCurrency);
        String transferAmount = "100";
        TransferRequestDTO transferRequestDTO = buildTransferRequestDTO(sourceAccountId, destinationAccountId, transferAmount);

        //when
        shouldFailTransferBadRequest(transferRequestDTO, HttpStatus.NOT_FOUND_404);

        //then
    }

    @Test
    public void should_fail_place_transfer_destination_invalid_amount_zero() {
        //given
        int sourceAccountId = 1;
        int destinationAccountId = 2;
        Currency expectedCurrency = Currency.EUR;
        shouldCreateAccount(sourceAccountId, "100", expectedCurrency);
        shouldCreateAccount(destinationAccountId, "100", expectedCurrency);
        String transferAmount = "0";
        TransferRequestDTO transferRequestDTO = buildTransferRequestDTO(sourceAccountId, destinationAccountId, transferAmount);

        //when
        shouldFailTransferBadRequest(transferRequestDTO, HttpStatus.BAD_REQUEST_400);

        //then
    }

    @Test
    public void should_fail_place_transfer_destination_invalid_amount_negative() {
        //given
        int sourceAccountId = 1;
        int destinationAccountId = 2;
        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(sourceAccountId, "100", expectedCurrency);
        shouldCreateAccount(destinationAccountId, "100", expectedCurrency);
        String transferAmount = "-100";
        TransferRequestDTO transferRequestDTO = buildTransferRequestDTO(sourceAccountId, destinationAccountId, transferAmount);

        //when
        shouldFailTransferBadRequest(transferRequestDTO, HttpStatus.BAD_REQUEST_400);

        //then
    }

    @Test
    public void should_fail_place_transfer_source_account_insufficient_funds() {
        //given
        int sourceAccountId = 1;
        int destinationAccountId = 2;
        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(sourceAccountId, "100", expectedCurrency);
        shouldCreateAccount(destinationAccountId, "100", expectedCurrency);
        String transferAmount = "100.01";
        TransferRequestDTO transferRequestDTO = buildTransferRequestDTO(sourceAccountId, destinationAccountId, transferAmount);

        //when
        shouldFailTransferBadRequest(transferRequestDTO, HttpStatus.BAD_REQUEST_400);

        //then
    }

    @Test @SuppressWarnings("Duplicates")
    public void should_retrieve_all_placed_transfers_same_currency() {
        //given
        int accountAId = 1;
        int accountBId = 2;
        int accountCId = 3;
        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(accountAId, "900.25", expectedCurrency);
        shouldCreateAccount(accountBId, "600.78", expectedCurrency);
        shouldCreateAccount(accountCId, "333.55", expectedCurrency);

        String transferAmountAtoB = "0.25";
        String transferAmountBtoC = "1.55";
        String transferAmountCtoA = "125.20";

        List<TransferRequestDTO> transfers = new ArrayList<TransferRequestDTO>() {{
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountBId, accountCId, transferAmountBtoC));
            add(buildTransferRequestDTO(accountBId, accountCId, transferAmountBtoC));
            add(buildTransferRequestDTO(accountBId, accountCId, transferAmountBtoC));
            add(buildTransferRequestDTO(accountCId, accountAId, transferAmountCtoA));
            add(buildTransferRequestDTO(accountCId, accountAId, transferAmountCtoA));

        }};

        //place all transfers
        transfers.parallelStream().forEach(this::shouldPlaceTransfer);

        //when
        MessageResponse<List<TransferDTO>> response = get(GET_ALL_TRANSFERS_URI).as(new TypeRef<MessageResponse<List<TransferDTO>>>() {});

        //then
        Assert.assertFalse(response.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
        Assert.assertEquals(transfers.size(), response.getBody().size());

        validateAccount(accountAId, "1149.65", expectedCurrency);
        validateAccount(accountBId, "597.13", expectedCurrency);
        validateAccount(accountCId, "87.80", expectedCurrency);
    }

    @Test @SuppressWarnings("Duplicates")
    public void should_retrieve_all_placed_transfers_by_account_id() {
        //given
        int accountAId = 1;
        int accountBId = 2;
        int accountCId = 3;
        Currency expectedCurrency = Currency.PLN;
        shouldCreateAccount(accountAId, "900.25", expectedCurrency);
        shouldCreateAccount(accountBId, "600.78", expectedCurrency);
        shouldCreateAccount(accountCId, "333.55", expectedCurrency);

        String transferAmountAtoB = "0.25";
        String transferAmountBtoC = "1.55";
        String transferAmountCtoA = "125.20";

        List<TransferRequestDTO> transfers = new ArrayList<TransferRequestDTO>() {{
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountAId, accountBId, transferAmountAtoB));
            add(buildTransferRequestDTO(accountBId, accountCId, transferAmountBtoC));
            add(buildTransferRequestDTO(accountBId, accountCId, transferAmountBtoC));
            add(buildTransferRequestDTO(accountBId, accountCId, transferAmountBtoC));
            add(buildTransferRequestDTO(accountCId, accountAId, transferAmountCtoA));
            add(buildTransferRequestDTO(accountCId, accountAId, transferAmountCtoA));

        }};

        //place all transfers
        transfers.parallelStream().forEach(this::shouldPlaceTransfer);

        //when
        MessageResponse<List<TransferDTO>> responseAccountA = given()
                .pathParam("accountId", accountAId).get(GET_ALL_TRANSFERS_BY_ACCOUNT_ID_URI).as(TRANSFER_LIST_RESPONSE_TYPE_REF);
        MessageResponse<List<TransferDTO>> responseAccountB = given()
                .pathParam("accountId", accountBId).get(GET_ALL_TRANSFERS_BY_ACCOUNT_ID_URI).as(TRANSFER_LIST_RESPONSE_TYPE_REF);
        MessageResponse<List<TransferDTO>> responseAccountC = given()
                .pathParam("accountId", accountCId).get(GET_ALL_TRANSFERS_BY_ACCOUNT_ID_URI).as(TRANSFER_LIST_RESPONSE_TYPE_REF);


        //then

        //validate accountA Transfers
        Assert.assertFalse(responseAccountA.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, responseAccountA.getStatusCode());
        Assert.assertEquals(transfers.stream()
                .filter(tfxDTO -> tfxDTO.getDestinationAccountId() == accountAId || tfxDTO.getSourceAccountId() == accountAId)
                .count(), responseAccountA.getBody().stream()
                .filter(tfxDTO -> tfxDTO.getDestinationAccountId() == accountAId || tfxDTO.getSourceAccountId() == accountAId)
                .count());

        //validate accountB Transfers
        Assert.assertFalse(responseAccountB.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, responseAccountB.getStatusCode());
        Assert.assertEquals(transfers.stream()
                .filter(tfxDTO -> tfxDTO.getDestinationAccountId() == accountBId || tfxDTO.getSourceAccountId() == accountBId)
                .count(), responseAccountB.getBody().stream()
                .filter(tfxDTO -> tfxDTO.getDestinationAccountId() == accountBId || tfxDTO.getSourceAccountId() == accountBId)
                .count());

        //validate accountC Transfers
        Assert.assertFalse(responseAccountC.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, responseAccountC.getStatusCode());
        Assert.assertEquals(transfers.stream()
                .filter(tfxDTO -> tfxDTO.getDestinationAccountId() == accountCId || tfxDTO.getSourceAccountId() == accountCId)
                .count(), responseAccountC.getBody().stream()
                .filter(tfxDTO -> tfxDTO.getDestinationAccountId() == accountCId || tfxDTO.getSourceAccountId() == accountCId)
                .count());

        validateAccount(accountAId, "1149.65", expectedCurrency);
        validateAccount(accountBId, "597.13", expectedCurrency);
        validateAccount(accountCId, "87.80", expectedCurrency);
    }

    @Test
    public void should_fail_retrieve_transfers_invalid_account_id() {
        //given
        String invalidAccountId = "k";

        //when
        MessageResponse<List<TransferDTO>> response = given().pathParam("accountId", invalidAccountId)
                .when().get(GET_ALL_TRANSFERS_BY_ACCOUNT_ID_URI).as(TRANSFER_LIST_RESPONSE_TYPE_REF);

        //then
        Assert.assertTrue(response.isFailure());
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    public void should_retrieve_empty_transfers_list_non_existent_account_id() {
        //given
        int nonExistentAccountId = 1;

        //when
        MessageResponse<List<TransferDTO>> response = given().pathParam("accountId", nonExistentAccountId)
                .when().get(GET_ALL_TRANSFERS_BY_ACCOUNT_ID_URI).as(TRANSFER_LIST_RESPONSE_TYPE_REF);

        //then
        Assert.assertFalse(response.isFailure());
        Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
        Assert.assertTrue(response.getBody().isEmpty());

    }

    /**
     * please note this is a summary test for the additional endpoint /transfers/:accountId
     * the unitary tests for the other endpoint /accounts/:accountId/transfers are declared above.
     */
    @Test
    public void should_retrieve_transfers_with_extended_endpoint_transfers() {
        //non existent account
        given().pathParam("accountId", 1)
            .when().get(GET_ALL_ACCOUNT_TRANSFERS)
            .then().assertThat().statusCode(HttpStatus.OK_200)
                .and().contentType(ContentType.JSON)
                .and().body("isFailure", equalTo(false))
                .and().body("body", Matchers.hasSize(0));

        //invalid input
        given().pathParam("accountId", "m")
                .when().get(GET_ALL_ACCOUNT_TRANSFERS)
                .then().assertThat().statusCode(HttpStatus.BAD_REQUEST_400)
                .and().contentType(ContentType.JSON)
                .and().body("isFailure", equalTo(true))
                .and().body("body", Matchers.nullValue());

        Currency expectedCurrency = Currency.GBP;
        shouldCreateAccount(1, "0", expectedCurrency);
        shouldCreateAccount(2, "50", expectedCurrency);
        shouldPlaceTransfer(buildTransferRequestDTO(2, 1, "25.40"));

        //valid transfers
        given().pathParam("accountId", 1)
            .when().get(GET_ALL_ACCOUNT_TRANSFERS)
            .then().assertThat().statusCode(HttpStatus.OK_200)
                .and().contentType(ContentType.JSON)
                .and().body("isFailure", equalTo(false))
                .and().body("body", Matchers.hasSize(1));

        given().pathParam("accountId", 2)
            .when().get(GET_ALL_ACCOUNT_TRANSFERS)
            .then().assertThat().statusCode(HttpStatus.OK_200)
                .and().contentType(ContentType.JSON)
                .and().body("isFailure", equalTo(false))
                .and().body("body", Matchers.hasSize(1));
    }

    private void validateAccount(int accountId, String initialBalance, Currency expectedCurrency) {
        given()
            .pathParam("accountId", accountId)
        .when()
            .get(GET_ACCOUNT_BY_ID_URI)
        .then()
            .assertThat()
                .statusCode(HttpStatus.OK_200)
            .and()
                .contentType(ContentType.JSON)
            .and()
                .body("isFailure", equalTo(false))
            .and()
                .body("body.balance", amountMatcher(initialBalance))
            .and()
                .body("body.id", equalTo(accountId))
            .and()
                .body("body.currencyCode", equalTo(expectedCurrency.code));
    }

    private void shouldCreateAccount(int expectedId, String initialBalance, Currency expectedCurrency) {
        given()
            .body(buildCreateAccountRequestBody(initialBalance, expectedCurrency.code))
        .when()
            .post(POST_ACCOUNT_URI)
        .then()
            .assertThat()
                .statusCode(HttpStatus.CREATED_201)
            .and()
                .contentType(ContentType.JSON)
            .and()
                .body("isFailure", equalTo(false))
            .and()
                .body("body.balance", amountMatcher(initialBalance))
            .and()
                .body("body.id", equalTo(expectedId))
            .and()
                .body("body.currencyCode", equalTo(expectedCurrency.code));
    }

    private void shouldPlaceTransfer(TransferRequestDTO transferRequestDTO) {
        given()
            .body(buildCreateTransferRequestBody(transferRequestDTO))
        .when()
            .post(POST_TRANSFER_URI)
        .then()
            .assertThat()
                .statusCode(HttpStatus.CREATED_201)
            .and()
                .contentType(ContentType.JSON)
            .and()
                .body("isFailure", equalTo(false))
            .and()
                .body("body.sourceAccountId", equalTo(Long.valueOf(transferRequestDTO.getSourceAccountId()).intValue()))
            .and()
                .body("body.destinationAccountId", equalTo(Long.valueOf(transferRequestDTO.getDestinationAccountId()).intValue()))
            .and()
                .body("body.amount", amountMatcher(transferRequestDTO.getAmount().toString()));
    }

    private void shouldFailTransferBadRequest(TransferRequestDTO transferRequestDTO, int expectedHttpStatus) {
        given()
            .body(buildCreateTransferRequestBody(transferRequestDTO))
        .when()
            .post(POST_TRANSFER_URI)
        .then()
            .assertThat()
                .statusCode(expectedHttpStatus)
            .and()
                .contentType(ContentType.JSON)
            .and()
                .body("isFailure", equalTo(true));
    }

    private String buildCreateTransferRequestBody(TransferRequestDTO transferRequest) {
        try {
            return jsonHelper.toJson(transferRequest);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private TransferRequestDTO buildTransferRequestDTO(int sourceAccountId, int destinationAccountId, String transferAmount) {
        return new TransferRequestDTO(sourceAccountId, destinationAccountId, new BigDecimal(transferAmount));
    }

    /**
     * Numbers should be compared to the java "float" primitive
     * For more information please refer to https://github.com/rest-assured/rest-assured/wiki/Usage#note-on-floats-and-doubles
     */
    private Matcher<Float> amountMatcher(String initialBalance) {
        return equalTo(Float.valueOf(initialBalance));
    }

    private String buildCreateAccountRequestBody(String initialBalance, String currencyCode) {
        try {
            return jsonHelper.toJson(new CreateAccountRequestDTO(new BigDecimal(initialBalance), currencyCode));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

}
