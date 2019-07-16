package uk.gov.hmcts.reform.datagenerator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@SuppressWarnings({"PMD", "checkstyle:hideutilityclassconstructor"})
public class DataGeneratorApplication {

    static class GeneratorConfig {
        private String baseDir = "/mnt/secrets/";

        private final String etlDbUrl;
        final String etlDbUser;
        final String etlDbPassword;
        private final String etlSql;

        GeneratorConfig(String baseDir) {
            if (baseDir != null) {
                this.baseDir = baseDir;
            }
            Config config = ConfigFactory.load();
            this.etlDbUrl = config.getString("etl-db-url");
            if (config.hasPath("etl-db-user-file")) {
                String etlDbUserFile = config.getString("etl-db-user-file");
                this.etlDbUser = readFirstLine(etlDbUserFile);
            } else {
                this.etlDbUser = config.getString("etl-db-user");
            }
            if (config.hasPath("etl-db-password-file")) {
                String etlDbPasswordFile = config.getString("etl-db-password-file");
                this.etlDbPassword = readFirstLine(etlDbPasswordFile);
            } else {
                this.etlDbPassword = config.getString("etl-db-password");
            }
            this.etlSql = config.getString("etl-sql");
        }

        GeneratorConfig() {
            this(null);
        }

        private String readFirstLine(String fileName) {
            try {
                return Files.readAllLines(Paths.get(baseDir, fileName))
                    .stream()
                    .findFirst()
                    .orElseThrow();
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
        try (QueryExecutor executor = new QueryExecutor(
                config.etlDbUrl, config.etlDbUser, config.etlDbPassword, config.etlSql)
            ) {
            executor.execute();
        }
    }

    public GeneratorConfig getConfig() {
        return config;
    }


    public static void main(String[] args) {
        new DataGeneratorApplication().run();
    }

}
