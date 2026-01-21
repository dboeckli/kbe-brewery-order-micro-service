package ch.dboeckli.springframeworkguru.kbe.order.services.services.testcomponents;

import ch.guru.springframework.kbe.lib.events.BeerOrderValidationResult;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sfg.brewery.queues.validate-order-result}")
    String validateOrderResultQueue;

    @JmsListener(destination = "${sfg.brewery.queues.validate-order}")
    public void listen(Message message) throws JMSException {
        log.info("Beer Order Validation Mock received request: {}", message);
        //  await().atLeast(1L, TimeUnit.SECONDS).until(() -> true);

        String jsonString = message.getBody(String.class);
        JsonNode event = objectMapper.readTree(jsonString);
        log.debug("Beer Order Validation Mock received request");

        JsonNode beerOrder = event.get("beerOrder");

        boolean isValid = true;
        boolean sendOrder = true;
        JsonNode orderId = beerOrder.get("id");

        if (beerOrder.get("customerRef") != null) {
            if (beerOrder.get("customerRef").asString().equals("fail-validation")) {
                isValid = false;
            } else if (beerOrder.get("customerRef").asString().equals("dont-validate")) {
                sendOrder = false;
            }
        }

        if (sendOrder) {
            jmsTemplate.convertAndSend(validateOrderResultQueue, BeerOrderValidationResult.builder()
                .beerOrderId(UUID.fromString(orderId.asString()))
                .isValid(isValid)
                .build());
        }
    }
}
