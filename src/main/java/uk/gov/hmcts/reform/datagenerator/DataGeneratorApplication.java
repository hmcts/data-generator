package uk.gov.hmcts.reform.datagenerator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;


@SuppressWarnings({"PMD", "checkstyle:hideutilityclassconstructor"})
public class DataGeneratorApplication {

    static class GeneratorConfig {
        private String baseDir = "/mnt/secrets/";
        private String dataDir = "/mnt/data/";

        final String etlDbUrl;
        final String etlDbUser;
        final String etlDbPassword;
        final Duration timeToRun;
        final String jurisdiction;
        final String caseType;
        final String caseData;

        GeneratorConfig(String baseDir) {
            if (baseDir != null) {
                this.baseDir = baseDir;
            }
            Config config = ConfigFactory.load();
            this.etlDbUrl = config.getString("etl-db-url");
            if (config.hasPath("etl-db-user-file")) {
                String etlDbUserFile = config.getString("etl-db-user-file");
                this.etlDbUser = readFirstLine(this.baseDir, etlDbUserFile);
            } else {
                this.etlDbUser = config.getString("etl-db-user");
            }
            if (config.hasPath("etl-db-password-file")) {
                String etlDbPasswordFile = config.getString("etl-db-password-file");
                this.etlDbPassword = readFirstLine(this.baseDir, etlDbPasswordFile);
            } else {
                this.etlDbPassword = config.getString("etl-db-password");
            }
            if (config.hasPath("time-to-run")) {
                this.timeToRun = config.getDuration("time-to-run");
            } else {
                this.timeToRun = Duration.ofMinutes(1);
            }
            if (config.hasPath("jurisdiction")) {
                this.jurisdiction = config.getString("jurisdiction");
            } else {
                this.jurisdiction = "PROBATE";
            }
            if (config.hasPath("case-type")) {
                this.caseType = config.getString("case-type");
            } else {
                this.caseType = "GrantOfRepresentation";
            }
            if (config.hasPath("case-data-file")) {
                this.caseData = readFile(dataDir, config.getString("case-data-file"));
            } else {
                this.caseData = "{\"caseType\": \"intestacy\", \"ihtFormId\": \"IHT123\", \"paperForm\": \"Yes\", \"willExists\": \"No\", \"caseMatches\": [], \"casePrinted\": \"Yes\", \"ihtNetValue\": \"5425245\", \"ihtGrossValue\": \"241254\", \"applicationType\": \"Personal\", \"dateOfDeathType\": \"diedOn\", \"deceasedAddress\": {\"County\": \"\", \"Country\": \"United Kingdom\", \"PostCode\": \"TS3 3TS\", \"PostTown\": \"London\", \"AddressLine1\": \"130 Test Road\", \"AddressLine2\": \"\", \"AddressLine3\": \"\"}, \"deceasedSurname\": \"TEST\", \"registryLocation\": \"London\", \"boSendToBulkPrint\": \"Yes\", \"deceasedForenames\": \"JOHN TEST\", \"extraCopiesOfGrant\": \"4\", \"boDocumentsUploaded\": [{\"id\": \"945bf0234-9d20-21da-04fa-123456789abc\", \"value\": {\"Comment\": \"Test doing his thing\", \"DocumentLink\": {\"document_url\": \"http://dm-store-perftest/documents/123412-1234-1234-123456789abc\", \"document_filename\": \"Mastering Test.pdf\", \"document_binary_url\": \"http://dm-store-perftest/documents/123412-1234-1234-123456789abc/binary\"}, \"DocumentType\": \"correspondence\"}}], \"deceasedDateOfBirth\": \"1912-10-17\", \"deceasedDateOfDeath\": \"2019-06-19\", \"otherExecutorExists\": \"No\", \"applyingAsAnAttorney\": \"No\", \"deceasedAnyOtherNames\": \"No\", \"ihtFormCompletedOnline\": \"No\", \"primaryApplicantAddress\": {\"County\": \"\", \"Country\": \"United Kingdom\", \"PostCode\": \"TN12 9TX\", \"PostTown\": \"Test\", \"AddressLine1\": \"123 Test Street\", \"AddressLine2\": \"\", \"AddressLine3\": \"Test\"}, \"primaryApplicantSurname\": \"TEST\", \"applicationSubmittedDate\": \"2019-07-10\", \"primaryApplicantHasAlias\": \"No\", \"primaryApplicantForenames\": \"JANE TEST\", \"probateDocumentsGenerated\": [], \"primaryApplicantIsApplying\": \"Yes\", \"boCaveatStopSendToBulkPrint\": \"Yes\", \"boCaveatStopEmailNotification\": \"No\", \"probateNotificationsGenerated\": [], \"boEmailGrantIssuedNotification\": \"No\", \"boEmailDocsReceivedNotification\": \"No\", \"boEmailDocsReceivedNotificationRequested\": \"No\"}";;
            }
        }

        GeneratorConfig() {
            this(null);
        }

        private String readFirstLine(String dir, String fileName) {
            try {
                return Files.readAllLines(Paths.get(dir, fileName))
                    .stream()
                    .findFirst()
                    .orElseThrow();
            } catch (IOException e) {
                throw new ExtractorException(e);
            }
        }

        private String readFile(String dir, String fileName) {
            try {
                return Files.readString(Paths.get(dir, fileName));
            } catch (IOException e) {
                throw new ExtractorException(e);
            }
        }
    }

    private GeneratorConfig config;

    public DataGeneratorApplication() {
        this.config = new GeneratorConfig();
    }

    DataGeneratorApplication(String baseDir) {
        this.config = new GeneratorConfig(baseDir);
    }

    public void run() {
        try (QueryExecutor executor = new QueryExecutor(config)
            ) {

            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plus(config.timeToRun);
            while (LocalDateTime.now().isBefore(end)) {
                executor.execute("insert into case_data (created_date, last_modified, jurisdiction, case_type_id, state, locked_by_user_id, data, data_classification, reference, security_classification)\n" +
                        "       values (?, ?, ?, ?, 'O', null, ?::jsonb, ?::jsonb, ?, 'Public');");
            }
        }
    }

    public GeneratorConfig getConfig() {
        return config;
    }


    public static void main(String[] args) {
        new DataGeneratorApplication().run();
    }

}
