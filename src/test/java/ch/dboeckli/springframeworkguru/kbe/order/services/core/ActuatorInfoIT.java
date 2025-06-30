package ch.dboeckli.springframeworkguru.kbe.order.services.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.wiremock.spring.EnableWireMock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock
@ActiveProfiles("wiremock")
@Slf4j
class ActuatorInfoIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuildProperties buildProperties;
    
    @Test
    void actuatorInfoTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.git.commit.id").isString())
            
            .andExpect(jsonPath("$.build.javaVersion").value("21"))
            .andExpect(jsonPath("$.build.commit-id").isString())
            .andExpect(jsonPath("$.build.javaVendor").isString())
            .andExpect(jsonPath("$.build.artifact").value(buildProperties.getArtifact()))
            .andExpect(jsonPath("$.build.group").value(buildProperties.getGroup()))
            .andReturn();
        
        log.info("Response: {}", result.getResponse().getContentAsString());
    }

    @Test
    void actuatorHealthTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/health/readiness"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andReturn();

        log.info("Response: {}", result.getResponse().getContentAsString());
    }

    
}
