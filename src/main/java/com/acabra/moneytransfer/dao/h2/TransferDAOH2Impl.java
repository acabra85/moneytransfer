package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.dto.TransferDTO;
import com.acabra.moneytransfer.model.TransferRequest;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

public class TransferDAOH2Impl implements TransferDAO {

    private final Sql2o sql2o;

    //DDL
    static final String CREATE_TABLE_TRANSFER =
            "CREATE TABLE transfer(" +
                "transfer_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "source_account_id BIGINT NOT NULL, " +
                "destination_account_id BIGINT NOT NULL, " +
                "transfer_amount DECIMAL(20, 4) NOT NULL, " +
                "transfer_timestamp TIMESTAMP NOT NULL, " +
                "FOREIGN KEY (source_account_id) REFERENCES account(account_id), " +
                "FOREIGN KEY (destination_account_id) REFERENCES account(account_id)" +
            ")";

    //DML
    private static final String STORE_TRANSFER_QUERY =
            "INSERT INTO transfer(transfer_timestamp, source_account_id, destination_account_id, transfer_amount) " +
                    "VALUES(:timestamp, :source, :destination, :amount)";

    private static final String RETRIEVE_TRANSFERS_BY_ACCOUNT_ID =
            "SELECT transfer_timestamp as timestamp, " +
                    "source_account_id as sourceAccountId, " +
                    "destination_account_id as destinationAccountId, " +
                    "transfer_amount as amount " +
            "FROM transfer " +
            "WHERE source_account_id = :id OR destination_account_id = :id";

    public TransferDAOH2Impl(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public void storeTransfer(TransferRequest request) {
        try (Connection cx = sql2o.open()) {
            cx.createQuery(STORE_TRANSFER_QUERY)
                    .addParameter("timestamp", request.timestamp)
                    .addParameter("source", request.sourceAccountId)
                    .addParameter("destination", request.destinationAccountId)
                    .addParameter("amount", request.transferAmount)
                    .executeUpdate();
        }
    }

    @Override
    public List<TransferDTO> retrieveTransfersByAccountId(long accountId) {
        try (Connection cx = sql2o.open()) {
            return cx.createQuery(RETRIEVE_TRANSFERS_BY_ACCOUNT_ID)
                    .addParameter("id", accountId)
                    .executeAndFetch(TransferDTO.class);
        }
    }
}
