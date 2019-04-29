package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import java.util.List;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

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
            "SELECT transfer_id as id, " +
                    "transfer_timestamp as timestamp, " +
                    "source_account_id as sourceAccountId, " +
                    "destination_account_id as destinationAccountId, " +
                    "transfer_amount as transferAmount " +
            "FROM transfer " +
            "WHERE source_account_id = :id OR destination_account_id = :id " +
            "ORDER BY transfer_id ASC";

    private static final String RETRIEVE_ALL_TRANSFERS =
            "SELECT transfer_id as id, " +
                    "transfer_timestamp as timestamp, " +
                    "source_account_id as sourceAccountId, " +
                    "destination_account_id as destinationAccountId, " +
                    "transfer_amount as transferAmount " +
                    "FROM transfer " +
                    "ORDER BY transfer_id ASC";

    public TransferDAOH2Impl(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Transfer storeTransferAndCommitTransactional(TransferRequest transferRequest, Connection tx) {
        Long id = tx.createQuery(STORE_TRANSFER_QUERY)
                .addParameter("timestamp", transferRequest.getTimestamp())
                .addParameter("source", transferRequest.getSourceAccountId())
                .addParameter("destination", transferRequest.getDestinationAccountId())
                .addParameter("amount", transferRequest.getTransferAmount())
                .executeUpdate().getKey(Long.class);
        tx.commit();
        return new Transfer(id, transferRequest.getTimestamp(), transferRequest.getSourceAccountId(), transferRequest.getDestinationAccountId(), transferRequest.getTransferAmount());
    }

    @Override
    public List<Transfer> retrieveTransfersByAccountId(long accountId) {
        try (Connection cx = sql2o.open()) {
            return cx.createQuery(RETRIEVE_TRANSFERS_BY_ACCOUNT_ID)
                    .addParameter("id", accountId)
                    .executeAndFetch(Transfer.class);
        }
    }

    @Override
    public List<Transfer> retrieveAllTransfers() {
        try (Connection cx = sql2o.open()) {
            return cx.createQuery(RETRIEVE_ALL_TRANSFERS)
                    .executeAndFetch(Transfer.class);
        }
    }
}
