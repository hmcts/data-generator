package uk.gov.hmcts.reform.datagenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;


public class QueryExecutor implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryExecutor.class);

    private Connection connection;
    final DataGeneratorApplication.GeneratorConfig config;
    private final Random random = new Random();

    public QueryExecutor(DataGeneratorApplication.GeneratorConfig config) {
        this.config = config;
        LOGGER.info("Connecting to db {}", config.etlDbUrl);
        try {
            this.connection = DriverManager.getConnection(config.etlDbUrl, config.etlDbUser, config.etlDbPassword);
            this.connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new ExecutorException(ex);
        }
    }


    @SuppressWarnings("squid:S2095")
    public boolean execute(String sql) {
        boolean result = false;
        PreparedStatement statement = null;
        final String dataClassification = "{\"caseType\": \"PUBLIC\", \"ihtFormId\": \"PUBLIC\", \"paperForm\": \"PUBLIC\", \"willExists\": \"PUBLIC\", \"caseMatches\": {\"value\": [], \"classification\": \"PUBLIC\"}, \"casePrinted\": \"PUBLIC\", \"ihtNetValue\": \"PUBLIC\", \"ihtGrossValue\": \"PUBLIC\", \"applicationType\": \"PUBLIC\", \"dateOfDeathType\": \"PUBLIC\", \"deceasedAddress\": {\"value\": {\"County\": \"PUBLIC\", \"Country\": \"PUBLIC\", \"PostCode\": \"PUBLIC\", \"PostTown\": \"PUBLIC\", \"AddressLine1\": \"PUBLIC\", \"AddressLine2\": \"PUBLIC\", \"AddressLine3\": \"PUBLIC\"}, \"classification\": \"PUBLIC\"}, \"deceasedSurname\": \"PUBLIC\", \"registryLocation\": \"PUBLIC\", \"boSendToBulkPrint\": \"PUBLIC\", \"deceasedForenames\": \"PUBLIC\", \"extraCopiesOfGrant\": \"PUBLIC\", \"boDocumentsUploaded\": {\"value\": [{\"id\": \"820f6c2b-9fa8-45c7-a6af-aca14b3823f4\", \"value\": {\"Comment\": \"PUBLIC\", \"DocumentLink\": \"PUBLIC\", \"DocumentType\": \"PUBLIC\"}}], \"classification\": \"PUBLIC\"}, \"deceasedDateOfBirth\": \"PUBLIC\", \"deceasedDateOfDeath\": \"PUBLIC\", \"otherExecutorExists\": \"PUBLIC\", \"applyingAsAnAttorney\": \"PUBLIC\", \"deceasedAnyOtherNames\": \"PUBLIC\", \"ihtFormCompletedOnline\": \"PUBLIC\", \"primaryApplicantAddress\": {\"value\": {\"County\": \"PUBLIC\", \"Country\": \"PUBLIC\", \"PostCode\": \"PUBLIC\", \"PostTown\": \"PUBLIC\", \"AddressLine1\": \"PUBLIC\", \"AddressLine2\": \"PUBLIC\", \"AddressLine3\": \"PUBLIC\"}, \"classification\": \"PUBLIC\"}, \"primaryApplicantSurname\": \"PUBLIC\", \"applicationSubmittedDate\": \"PUBLIC\", \"primaryApplicantHasAlias\": \"PUBLIC\", \"primaryApplicantForenames\": \"PUBLIC\", \"probateDocumentsGenerated\": {\"value\": [], \"classification\": \"PUBLIC\"}, \"primaryApplicantIsApplying\": \"PUBLIC\", \"boCaveatStopSendToBulkPrint\": \"PUBLIC\", \"boCaveatStopEmailNotification\": \"PUBLIC\", \"probateNotificationsGenerated\": {\"value\": [], \"classification\": \"PUBLIC\"}, \"boEmailGrantIssuedNotification\": \"PUBLIC\", \"boEmailDocsReceivedNotification\": \"PUBLIC\", \"boEmailDocsReceivedNotificationRequested\": \"PUBLIC\"}";
        try {
            LOGGER.info("Executing sql...");
            statement = this.connection.prepareStatement(sql);
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString( 3, config.jurisdiction);
            statement.setString( 4, config.caseType);
            statement.setString(5, config.caseData);
            statement.setString(6, dataClassification);
            statement.setInt(7, random.nextInt());

            long startTime = System.nanoTime();
            result = statement.execute();
            this.connection.commit();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            LOGGER.info("Done. Execution time: {} ms", duration);
        } catch (SQLException ex) {
            LOGGER.warn("SQL Exception thrown while executing statement.", ex);
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
