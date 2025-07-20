package ch.dboeckli.springframeworkguru.kbe.order.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.test.config.DockerComposeInitializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ContextConfiguration(initializers = DockerComposeInitializer.class) // this ensures that Docker Compose from compose-wiremock.yaml is stopped before running the test
@Slf4j
class BreweryOrderServiceIT {

    @Autowired
    private ApplicationContext applicationContext;


    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
        log.info("Testing Spring 6 Application {}", applicationContext.getApplicationName());
    }

}
