[![Build Status](https://travis-ci.org/acabra85/moneytransfer.svg?branch=master)](https://travis-ci.org/acabra85/moneytransfer)
[![codecov](https://codecov.io/gh/acabra85/moneytransfer/branch/master/graph/badge.svg)](https://codecov.io/gh/acabra85/moneytransfer)

# moneytransfer
Powerful backend http server with intuitive rest api, in memory DB for transfer money between accounts

## Run
Download the source code and execute the following command on the root folder 'moneytransfer'.

##### Note: Make sure you have java 1.8 or higher installed.

1. For non-windows users ``` ./mvnw clean install; java -jar target/moneytransfer-1.0-SNAPSHOT.jar ```
1. For windows users ```mvnw.exe clean install; java -jar target/moneytransfer-1.0-SNAPSHOT.jar```

An http server will be listening on http://127.0.0.1:4567

### Test Coverage
To see the test coverage report head to target/site/jacoco/index.html (or click the codecov link above)

## API
* ``` /api/accounts ``` [GET] List all accounts
* ``` /api/accounts/:accountId ``` [GET] Account with id (:accountId)
* ``` /api/accounts/:accountId/transfers``` [GET] List all transfers involving the account with given path param (:accountId)
* ``` /api/accounts/new``` [POST] Creates a new account, the request body should contain a field 'initialBalance'
```json 
{
    "initialBalance": 200.10
}
```
*  ```/api/transfers``` [GET] List all transfers
* ``` /api/transfers/:accountId ``` [GET] List all transfers for the account with given path param (:accountId)
* ``` /api/transfers/new ``` [POST] Create a new transfer, the request body should contain the fields 'sourceAccountId' 'destinationAccountId' and 'amount'
```json 
{
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 200.10
}
```

### Response
Every response is a JSON object with the following format:
* id: identifier of the response
* statusCode: http status code of the respose,
* isFailure: a boolean denoting if the call failed
* message: a description of the response
* body: The object response for successful requests.

Example
```json 
{
    "id": 1,
    "statusCode": 200,
    "isFailure": false,
    "message": "Success retrieving accounts",
    "body": []
}
```

### Want to test out of the box with POSTMAN?

1. There is a collection ready for import with predefined examples for all available endpoints in the folder postman/MoneyTransfer.postman_collection.json, 
and the environment postman/Local.postman_environment.json.

## Libraries Used

* h2 (in memory db)
* sql2o (db connector object mapper)
* slf4j (logging)
* fasterxml/jackson (json parsing)
* junit rest-assured mockito jacoco (testing reporting)
* javaspark (jetty http server and endpoint definitions for REST)

## Summary
Transfer requests are handled atomically using pessimistic locking at the db-rows level, and at the 
Account object level with ReentrantLock to guarantee atomic transactions.

