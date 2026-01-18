package ch.dboeckli.springframeworkguru.kbe.order.services.services.testcomponents;

import ch.dboeckli.springframeworkguru.kbe.order.services.config.JmsConfig;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.repositories.BeerOrderRepository;
import ch.dboeckli.springframeworkguru.kbe.order.services.web.mappers.BeerOrderMapper;
import ch.guru.springframework.kbe.lib.events.AllocateBeerOrderResult;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message message) throws JMSException {
        log.info("Beer Order Allocation Mock received request: {}", message);
        String jsonString = message.getBody(String.class);
        JsonNode event = objectMapper.readTree(jsonString);
        JsonNode beerOrder = event.get("beerOrder");

        boolean allocationError = false;
        boolean sendOrder = true;
        JsonNode orderId = beerOrder.get("id");
        BeerOrder beerOrderFromDB = beerOrderRepository.findById(UUID.fromString(orderId.asString())).get();

        if (beerOrder.get("customerRef") != null && !StringUtils.isBlank(beerOrder.get("customerRef").asString())) {
            log.info("########################################");
            log.info("customerRef {}", beerOrder.get("customerRef").asString());
            log.info("########################################");
            if (beerOrder.get("customerRef").asString().equals("allocation-fail")) {
                allocationError = true;
            } else if (beerOrder.get("customerRef").asString().equals("dont-allocate")) {
                sendOrder = false;
            }
        }

        if (sendOrder) {
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE, AllocateBeerOrderResult.builder()
                .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrderFromDB))
                .allocationError(allocationError)
                .pendingInventory(false)
                .build());
        }
    }
}
