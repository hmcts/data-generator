package uk.gov.hmcts.reform.datagenerator;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class DataGeneratorApplicationTest {

    @Test
    public void whenApplicationCreated_thenConfigurationRead() {
        ConfigFactory.invalidateCaches();
        System.setProperty("config.resource", "application-alt.conf");
        DataGeneratorApplication application = new DataGeneratorApplication();
        assertNotNull(application.getConfig());
        assertEquals("user", application.getConfig().etlDbUser);
        assertEquals("password", application.getConfig().etlDbPassword);
    }

    @Test
    public void whenDbUserAndPasswordAreFiles_thenFilesAreRead(@TempDir Path tempDir) throws Exception {
        Files.write(tempDir.resolve("user-file"), "username".getBytes());
        Files.write(tempDir.resolve("password-file"), "password1\npassword2".getBytes());
        String baseDir = tempDir.normalize().toString();
        DataGeneratorApplication application = new DataGeneratorApplication(baseDir);
        assertEquals("username", application.getConfig().etlDbUser);
        assertEquals("password1", application.getConfig().etlDbPassword);
    }

}
