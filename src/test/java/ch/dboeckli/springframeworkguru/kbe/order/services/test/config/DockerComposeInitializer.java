package ch.dboeckli.springframeworkguru.kbe.order.services.test.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;

@Slf4j
public class DockerComposeInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("Shutting down existing Docker environment...");
        try {
            String composeFilePath = System.getProperty("docker.compose.file",
                System.getProperty("user.dir") + "/compose-wiremock.yaml");

            log.info("Using Docker Compose file: {}", composeFilePath);

            File composeFile = new File(composeFilePath);
            if (!composeFile.exists()) {
                throw new RuntimeException("Docker Compose file not found: " + composeFilePath);
            }

            ProcessBuilder pb = new ProcessBuilder(
                "docker", "compose",
                "-f", composeFilePath,
                "down"
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("docker compose down failed");
            } else {
                log.info("Docker environment shut down successfully");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to shut down Docker environment", e);
        }
    }
}