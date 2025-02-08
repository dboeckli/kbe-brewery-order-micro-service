package ch.dboeckli.springframeworkguru.kbe.order.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@DirtiesContext
@ActiveProfiles("test")
@Slf4j
class BreweryOrderServiceTest {

    @Test
    void contextLoads() {
        log.info("Testing Spring 6 Application...");
    }

}
