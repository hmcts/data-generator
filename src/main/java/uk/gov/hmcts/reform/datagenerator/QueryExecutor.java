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
        boolean result = false;
        PreparedStatement statement = null;
        Random random = new Random();
        String caseData = "{\"caseType\": \"intestacy\", \"ihtFormId\": \"IHT123\", \"paperForm\": \"Yes\", \"willExists\": \"No\", \"caseMatches\": [], \"casePrinted\": \"Yes\", \"ihtNetValue\": \"5425245\", \"ihtGrossValue\": \"241254\", \"applicationType\": \"Personal\", \"dateOfDeathType\": \"diedOn\", \"deceasedAddress\": {\"County\": \"\", \"Country\": \"United Kingdom\", \"PostCode\": \"TS3 3TS\", \"PostTown\": \"London\", \"AddressLine1\": \"130 Test Road\", \"AddressLine2\": \"\", \"AddressLine3\": \"\"}, \"deceasedSurname\": \"TEST\", \"registryLocation\": \"London\", \"boSendToBulkPrint\": \"Yes\", \"deceasedForenames\": \"JOHN TEST\", \"extraCopiesOfGrant\": \"4\", \"boDocumentsUploaded\": [{\"id\": \"945bf0234-9d20-21da-04fa-123456789abc\", \"value\": {\"Comment\": \"Test doing his thing\", \"DocumentLink\": {\"document_url\": \"http://dm-store-perftest/documents/123412-1234-1234-123456789abc\", \"document_filename\": \"Mastering Test.pdf\", \"document_binary_url\": \"http://dm-store-perftest/documents/123412-1234-1234-123456789abc/binary\"}, \"DocumentType\": \"correspondence\"}}], \"deceasedDateOfBirth\": \"1912-10-17\", \"deceasedDateOfDeath\": \"2019-06-19\", \"otherExecutorExists\": \"No\", \"applyingAsAnAttorney\": \"No\", \"deceasedAnyOtherNames\": \"No\", \"ihtFormCompletedOnline\": \"No\", \"primaryApplicantAddress\": {\"County\": \"\", \"Country\": \"United Kingdom\", \"PostCode\": \"TN12 9TX\", \"PostTown\": \"Test\", \"AddressLine1\": \"123 Test Street\", \"AddressLine2\": \"\", \"AddressLine3\": \"Test\"}, \"primaryApplicantSurname\": \"TEST\", \"applicationSubmittedDate\": \"2019-07-10\", \"primaryApplicantHasAlias\": \"No\", \"primaryApplicantForenames\": \"JANE TEST\", \"probateDocumentsGenerated\": [], \"primaryApplicantIsApplying\": \"Yes\", \"boCaveatStopSendToBulkPrint\": \"Yes\", \"boCaveatStopEmailNotification\": \"No\", \"probateNotificationsGenerated\": [], \"boEmailGrantIssuedNotification\": \"No\", \"boEmailDocsReceivedNotification\": \"No\", \"boEmailDocsReceivedNotificationRequested\": \"No\"}";
        String dataClassification = "{\"caseType\": \"PUBLIC\", \"ihtFormId\": \"PUBLIC\", \"paperForm\": \"PUBLIC\", \"willExists\": \"PUBLIC\", \"caseMatches\": {\"value\": [], \"classification\": \"PUBLIC\"}, \"casePrinted\": \"PUBLIC\", \"ihtNetValue\": \"PUBLIC\", \"ihtGrossValue\": \"PUBLIC\", \"applicationType\": \"PUBLIC\", \"dateOfDeathType\": \"PUBLIC\", \"deceasedAddress\": {\"value\": {\"County\": \"PUBLIC\", \"Country\": \"PUBLIC\", \"PostCode\": \"PUBLIC\", \"PostTown\": \"PUBLIC\", \"AddressLine1\": \"PUBLIC\", \"AddressLine2\": \"PUBLIC\", \"AddressLine3\": \"PUBLIC\"}, \"classification\": \"PUBLIC\"}, \"deceasedSurname\": \"PUBLIC\", \"registryLocation\": \"PUBLIC\", \"boSendToBulkPrint\": \"PUBLIC\", \"deceasedForenames\": \"PUBLIC\", \"extraCopiesOfGrant\": \"PUBLIC\", \"boDocumentsUploaded\": {\"value\": [{\"id\": \"820f6c2b-9fa8-45c7-a6af-aca14b3823f4\", \"value\": {\"Comment\": \"PUBLIC\", \"DocumentLink\": \"PUBLIC\", \"DocumentType\": \"PUBLIC\"}}], \"classification\": \"PUBLIC\"}, \"deceasedDateOfBirth\": \"PUBLIC\", \"deceasedDateOfDeath\": \"PUBLIC\", \"otherExecutorExists\": \"PUBLIC\", \"applyingAsAnAttorney\": \"PUBLIC\", \"deceasedAnyOtherNames\": \"PUBLIC\", \"ihtFormCompletedOnline\": \"PUBLIC\", \"primaryApplicantAddress\": {\"value\": {\"County\": \"PUBLIC\", \"Country\": \"PUBLIC\", \"PostCode\": \"PUBLIC\", \"PostTown\": \"PUBLIC\", \"AddressLine1\": \"PUBLIC\", \"AddressLine2\": \"PUBLIC\", \"AddressLine3\": \"PUBLIC\"}, \"classification\": \"PUBLIC\"}, \"primaryApplicantSurname\": \"PUBLIC\", \"applicationSubmittedDate\": \"PUBLIC\", \"primaryApplicantHasAlias\": \"PUBLIC\", \"primaryApplicantForenames\": \"PUBLIC\", \"probateDocumentsGenerated\": {\"value\": [], \"classification\": \"PUBLIC\"}, \"primaryApplicantIsApplying\": \"PUBLIC\", \"boCaveatStopSendToBulkPrint\": \"PUBLIC\", \"boCaveatStopEmailNotification\": \"PUBLIC\", \"probateNotificationsGenerated\": {\"value\": [], \"classification\": \"PUBLIC\"}, \"boEmailGrantIssuedNotification\": \"PUBLIC\", \"boEmailDocsReceivedNotification\": \"PUBLIC\", \"boEmailDocsReceivedNotificationRequested\": \"PUBLIC\"}";
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
