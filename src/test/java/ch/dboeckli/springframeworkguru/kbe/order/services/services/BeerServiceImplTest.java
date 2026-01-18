package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.services.beer.BeerServiceImpl;
import ch.guru.springframework.kbe.lib.dto.BeerDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BeerServiceImpl.class)
@ActiveProfiles("test")
class BeerServiceImplTest {

    @Autowired
    BeerServiceImpl beerService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockRestServiceServer server;

    @Test
    void getBeerById() {
        //given
        UUID testUUID = UUID.randomUUID();
        BeerDto dto = BeerDto.builder().id(testUUID).build();
        String jsonDto = mapper.writeValueAsString(dto);

        server.expect(requestTo("http://localhost:8083/api/v1/beer/" + testUUID))
            .andRespond(withSuccess(jsonDto, MediaType.APPLICATION_JSON));

        Optional<BeerDto> beerDtoOptional = beerService.getBeerById(testUUID);

        assertTrue(beerDtoOptional.isPresent());
    }
}
