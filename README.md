# moneytransfer
Powerful backend http server with intuitive rest api, in memory DB for transfer money between accounts

## Run
Download the source code and execute the following command on the root folder 'moneytransfer'.

1. For non-windows users ``` ./mvnw clean install; java -jar target/moneytransfer-1.0-SNAPSHOT.jar ```
1. For windows users ```mvnw.exe clean install; java -jar target/moneytransfer-1.0-SNAPSHOT.jar```

An http server will be listening http://127.0.0.1:4567

## API
* ``` /api/accounts ``` [GET] List all accounts
* ``` /api/accounts/:accountId ``` [GET] Account with id (:accountId)
* ``` /api/accounts/:accountId/transfers``` [GET] List all transfers involving the account with given path param (:accountId)
* ``` /api/accounts/new``` [POST] Creates a new account, the request body should contain a field 'initialBalance'
```json 
{
    'initialBalance': 200.10
}
```
*  ```/api/transfers``` [GET] List all transfers
* ``` /api/transfers/:accountId ``` [GET] List all transfers for the account with given path param (:accountId)
* ``` /api/transfers/new ``` [POST] Create a new transfer, the request body should contain the fields 'sourceAccountId' 'destinationAccountId' and 'amount'
```json 
{
    'sourceAccountId': 1,
    'destinationAccountId': 2,
    'amount': 200.10
}
```

### Response
All response is JSON objects with the following format:
```json 
{
    id: #identifier of the response,
    message: a description of the response
    isFailure: a boolean denoting if the call failed
    body: an optional value containing the JSON object response for successful requests.
}
```

### POSTMAN

1. There is a collection ready for import with predefined examples for all available endpoints in the folder postman/MoneyTransfer.postman_collection.json, 
and the environment postman/Local.postman_environment.json.

## Libraries Used

* h2 (in memory db)
* gson (json parsing)
* junit rest-assured mockito (testing)
* javaspark (jetty http server and enpoint definitions for REST)

## Summary
Transfer request handled atomically using pessimistic locking at the db-rows, and the 
Account objects with ReentrantLock to guarantee an atomic withdraw and deposit.

