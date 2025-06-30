package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.config.JmsConfig;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderLine;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderStatusEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.Customer;
import ch.dboeckli.springframeworkguru.kbe.order.services.repositories.BeerOrderRepository;
import ch.dboeckli.springframeworkguru.kbe.order.services.repositories.CustomerRepository;
import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.dto.BeerPagedList;
import ch.guru.springframework.kbe.lib.events.AllocationFailureEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@EnableWireMock({
    @ConfigureWireMock(
        filesUnderDirectory = "src/test/resources/wiremock"
    )
})
@ActiveProfiles("wiremock")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class BeerOrderManagerImplWiremockTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${wiremock.server.baseUrl}")
    private String wireMockUrl;

    @Value("${wiremock.server.port}")
    private String wireMockPort;

    @InjectWireMock
    WireMockServer wireMockServer;

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    JmsTemplate jmsTemplate;

    Customer testCustomer;

    UUID beerIdFirst = UUID.randomUUID();
    UUID beerIdSecond = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
        checkWireMockServer();
    }

    @Test
    @Order(1)
    void testNewToAllocate() throws JsonProcessingException {
        createBeerStubs();
        BeerOrder orderToSave = createBeerOrder();

        BeerOrder savedOrder = beerOrderManager.newBeerOrder(orderToSave);
        AtomicReference<BeerOrder> foundBeerOrderRef = new AtomicReference<>();
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.getReferenceById(savedOrder.getId());
            foundBeerOrderRef.set(foundOrder);
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });
        BeerOrder foundOrder = foundBeerOrderRef.get();
        assertNotNull(foundOrder);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
    }

    @Test
    @Order(2)
    void testGoodOrderHappyPath() throws JsonProcessingException {
        createBeerStubs();
        BeerOrder orderToSave = createBeerOrder();

        BeerOrder savedOrder = beerOrderManager.newBeerOrder(orderToSave);
        AtomicReference<BeerOrder> foundBeerOrderRef = new AtomicReference<>();
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.getReferenceById(savedOrder.getId());
            foundBeerOrderRef.set(foundOrder);
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus()); 
        });
        BeerOrder foundOrder = foundBeerOrderRef.get();
        assertNotNull(foundOrder);
        
        //pickup order
        beerOrderManager.pickupBeerOrder(foundOrder.getId());
        await().untilAsserted(() -> {
            BeerOrder orderCheck = beerOrderRepository.getReferenceById(savedOrder.getId());
            assertEquals(BeerOrderStatusEnum.PICKED_UP, orderCheck.getOrderStatus()); 
        });
    }

    @Test
    @Order(3)
    void testBeerOrderFailedValidation() throws JsonProcessingException {
        createBeerStubs();
        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("fail-validation");

        beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());  
        });
    }

    @Test
    @Order(4)
    void testBeerOrderAllocationFailed() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerIdFirst).upc("1234").build();
        stubFor(get(BeerServiceImpl.BEER_PATH_V1 + beerIdFirst.toString()).willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder orderToSave = createBeerOrder();
        orderToSave.setCustomerRef("allocation-fail");

        BeerOrder savedOrder = beerOrderManager.newBeerOrder(orderToSave);
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.getReferenceById(savedOrder.getId());
            assertEquals(BeerOrderStatusEnum.ALLOCATION_ERROR, foundOrder.getOrderStatus()); 
        });

        AllocationFailureEvent event = (AllocationFailureEvent) jmsTemplate.receiveAndConvert(JmsConfig.ALLOCATION_FAILURE_QUEUE);
        assertThat(event.getBeerOrderId()).isEqualTo(savedOrder.getId());
    }

    @Test
    @Order(5)
    void testPickupBeerOrder() throws JsonProcessingException {
        createBeerStubs();
        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());  
        });

        beerOrderManager.pickupBeerOrder(savedBeerOrder.getId());

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus()); 
        });
        BeerOrder pickedUpOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
        assertEquals(BeerOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus()); 
    }

    private BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();
        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder().beerId(beerIdFirst.toString()).orderQuantity(1).beerOrder(beerOrder).build());
        beerOrder.setBeerOrderLines(lines);
        return beerOrder;
    }

    private void checkWireMockServer() {
        log.info("### WireMock server on base url {} running at url {} on port {}", wireMockServer.baseUrl(), wireMockUrl, wireMockPort);

        List<StubMapping> mappings =  wireMockServer.getStubMappings();
        Options options = wireMockServer.getOptions();
        log.info("Wiremock options: {}", options.filesRoot().getPath());
        log.info("Total number of stub mappings: {}", mappings.size());
        assertEquals(4, mappings.size());
        mappings.forEach(mapping -> log.info("### Stub Mapping: URL: {}, Method: {}, Response: Status {}, Body: {}",
            mapping.getRequest().getUrl(),
            mapping.getRequest().getMethod(),
            mapping.getResponse().getStatus(),
            mapping.getResponse().getBody()));
        
        String url = wireMockUrl + "/__admin/";
        RestTemplate restTemplate = restTemplateBuilder.build();
        HttpStatusCode statusCode = restTemplate.getForEntity(url, String.class).getStatusCode();
        
        assertThat(statusCode).isEqualTo(HttpStatus.OK);
    }

    private void createBeerStubs() throws JsonProcessingException {
        BeerDto beerDtoFirst  = BeerDto.builder().id(beerIdFirst).upc("1234").build();
        BeerDto beerDtoSecond = BeerDto.builder().id(beerIdSecond).upc("5678").build();

        stubFor(get(BeerServiceImpl.BEER_PATH_V1 + beerIdFirst.toString()).willReturn(okJson(objectMapper.writeValueAsString(beerDtoFirst))));
        log.info("#### Stub created for : " + BeerServiceImpl.BEER_PATH_V1 + beerIdFirst.toString());
        stubFor(get(BeerServiceImpl.BEER_PATH_V1 + beerIdSecond.toString()).willReturn(okJson(objectMapper.writeValueAsString(beerDtoSecond))));
        log.info("#### Stub created for : " + BeerServiceImpl.BEER_PATH_V1 + beerIdSecond.toString());

        // Create a list of BeerDto object
        BeerPagedList beerPagedList = new BeerPagedList(Arrays.asList(
            BeerDto.builder().id(beerIdFirst).upc(beerDtoFirst.getUpc()).build(),
            BeerDto.builder().id(beerIdSecond).upc(beerDtoSecond.getUpc()).build()),
            PageRequest.of(0, 25), 2);

        stubFor(get(BeerServiceImpl.LIST_BEER_PATH_V1).willReturn(okJson(objectMapper.writeValueAsString(beerPagedList))));
        log.info("#### Stub created for : " + BeerServiceImpl.LIST_BEER_PATH_V1);

        // Now we are printing out all stubs:
        log.info("### Registered stubs:");
        wireMockServer.getStubMappings().forEach(stub ->
            log.info("Stub: " + stub.getRequest().getUrl() + " -> " + stub.getResponse().getStatus())
        );
    }
}
