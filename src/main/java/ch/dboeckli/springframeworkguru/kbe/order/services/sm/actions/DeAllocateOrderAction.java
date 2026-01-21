package ch.dboeckli.springframeworkguru.kbe.order.services.sm.actions;

import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderEventEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderStatusEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.web.mappers.BeerOrderMapper;
import ch.guru.springframework.kbe.lib.events.DeAllocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static ch.dboeckli.springframeworkguru.kbe.order.services.services.beerorder.BeerOrderManagerImpl.ORDER_OBJECT_HEADER;


/**
 * Created by jt on 2/29/20.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DeAllocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Value("${sfg.brewery.queues.deallocate-order}")
    String deAllocateOrderQueue;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

        BeerOrder beerOrder = context.getStateMachine().getExtendedState()
            .get(ORDER_OBJECT_HEADER, BeerOrder.class);


        jmsTemplate.convertAndSend(deAllocateOrderQueue, DeAllocateOrderRequest.builder()
            .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
            .build());

        log.info("Sent request to queue " + deAllocateOrderQueue + " for Beer Order Id: " + beerOrder.getId().toString());
    }
}
