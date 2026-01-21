package ch.dboeckli.springframeworkguru.kbe.order.services.services.listeners;

import ch.dboeckli.springframeworkguru.kbe.order.services.services.beerorder.BeerOrderManager;
import ch.guru.springframework.kbe.lib.events.BeerOrderValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by jt on 2019-09-08.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = "${sfg.brewery.queues.validate-order-result}")
    public void listen(BeerOrderValidationResult result) {
        log.info("Validation Result for Beer Order: {}", result);
        final UUID beerOrderId = result.getBeerOrderId();
        if (result.getIsValid()) {
            beerOrderManager.beerOrderPassedValidation(beerOrderId);
        } else {
            beerOrderManager.beerOrderFailedValidation(beerOrderId);
        }
    }
}
