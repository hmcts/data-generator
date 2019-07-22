package uk.gov.hmcts.reform.datagenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;


public class QueryExecutor implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryExecutor.class);

    private Connection connection;

    public QueryExecutor(String jdbcUrl, String user, String password) {

        LOGGER.info("Connecting to db {}", jdbcUrl);
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, user, password);
            this.connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new ExecutorException(ex);
        }
    }


    @SuppressWarnings("squid:S2095")
    public boolean execute(String sql) {
        boolean result;
        PreparedStatement statement = null;
        Random random = new Random();
        String caseData = "{\"caseName\":\"CaseName1\",\n" +
                "\n" +
                "  \"caseStartDate\":\"01-Nov-2017\",\n" +
                "\n" +
                "  \"caseUpdateNo\":\"0\",\n" +
                "\n" +
                "  \"caseStatus\":\"Open\",\n" +
                "\n" +
                "  \"caseAddress\":{\n" +
                "\n" +
                "                            \"houseNumberName\":\"1\",\n" +
                "\n" +
                "                            \"postCode\":\"WV5 0DH\"\n" +
                "\n" +
                "                          }\n" +
                "\n" +
                "}";
        String dataClassification = "{\"classification\":\"data\"}";
        try {
            LOGGER.info("Executing sql...");
            statement = this.connection.prepareStatement(sql);
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(3, caseData);
            statement.setString(4, dataClassification);
            statement.setInt(5, random.nextInt());

            long startTime = System.nanoTime();
            result = statement.execute();
            this.connection.commit();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            LOGGER.info("Done. Execution time: {} ms", duration);
        } catch (SQLException ex) {
            throw new ExecutorException(ex);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                LOGGER.warn("SQL Exception thrown while closing statement.", ex);
            }
        }
        return result;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.warn("SQL Exception thrown while closing connection.", e);
        }
    }

}
