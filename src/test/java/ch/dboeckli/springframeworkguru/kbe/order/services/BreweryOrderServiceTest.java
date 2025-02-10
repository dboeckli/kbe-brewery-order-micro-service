package ch.dboeckli.springframeworkguru.kbe.order.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@DirtiesContext
@ActiveProfiles("test")
@Slf4j
class BreweryOrderServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
        log.info("Testing Spring 6 Application {}", applicationContext.getApplicationName());
    }

}
