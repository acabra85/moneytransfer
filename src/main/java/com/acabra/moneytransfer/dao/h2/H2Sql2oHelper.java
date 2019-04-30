package com.acabra.moneytransfer.dao.h2;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.quirks.Quirks;

public class H2Sql2oHelper {

    //DB CONFIG
    private static final Quirks NO_QUIRKS = new NoQuirks(new HashMap<Class, Converter>(){{
        put(LocalDateTime.class, new LocalDateTimeConverter());
    }});

    private static final String KEEP_DB_OPEN_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;LOCK_MODE=3";

    /**
     * Creates a db instance of sql2o for h2 db that remains open even if all connections have closed
     */
    public static Sql2o ofLocalKeepOpenSql2o() {
        Sql2o sql2o = new Sql2o(KEEP_DB_OPEN_URL, "", "", NO_QUIRKS);
        initializeDB(sql2o);
        return sql2o;
    }

    public static void initializeDB(Sql2o sql2o) {
        try(Connection tx = sql2o.beginTransaction()) {
            tx.createQuery(AccountDAOH2Impl.CLEAN_DB).executeUpdate();
            tx.createQuery(AccountDAOH2Impl.CREATE_TABLE_ACCOUNT).executeUpdate();
            tx.createQuery(TransferDAOH2Impl.CREATE_TABLE_TRANSFER).executeUpdate();
            tx.commit();
        }
    }

    private static class LocalDateTimeConverter implements Converter<LocalDateTime> {
        @Override
        public LocalDateTime convert(Object val) throws ConverterException {
            return ((Timestamp) val).toLocalDateTime();
        }

        @Override
        public Object toDatabaseParam(LocalDateTime val) {
            return Timestamp.valueOf(val);
        }
    }
}
