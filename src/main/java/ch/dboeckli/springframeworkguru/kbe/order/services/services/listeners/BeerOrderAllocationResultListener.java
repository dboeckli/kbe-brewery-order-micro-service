package ch.dboeckli.springframeworkguru.kbe.order.services.services.listeners;

import ch.dboeckli.springframeworkguru.kbe.order.services.services.beerorder.BeerOrderManager;
import ch.guru.springframework.kbe.lib.events.AllocateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2019-09-09.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationResultListener {
    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = "${sfg.brewery.queues.allocate-order-result}")
    public void listen(AllocateBeerOrderResult result) {

        log.info("Validation Result for Allocated Beer Order: {}", result);

        if (!result.getAllocationError() && !result.getPendingInventory()) {
            //allocated normally
            beerOrderManager.beerOrderAllocationPassed(result.getBeerOrderDto());
        } else if (!result.getAllocationError() && result.getPendingInventory()) {
            //pending inventory
            beerOrderManager.beerOrderAllocationPendingInventory(result.getBeerOrderDto());
        } else if (result.getAllocationError()) {
            //allocation error
            beerOrderManager.beerOrderAllocationFailed(result.getBeerOrderDto());
        }
    }
}
