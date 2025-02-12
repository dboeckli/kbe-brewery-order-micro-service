package ch.dboeckli.springframeworkguru.kbe.order.services.sm.actions;

import ch.dboeckli.springframeworkguru.kbe.order.services.config.JmsConfig;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderEventEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderStatusEnum;
import ch.guru.springframework.kbe.lib.events.AllocationFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static ch.dboeckli.springframeworkguru.kbe.order.services.services.BeerOrderManagerImpl.ORDER_OBJECT_HEADER;

/**
 * Created by jt on 2/25/20.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

        BeerOrder beerOrder = context.getStateMachine().getExtendedState()
                .get(ORDER_OBJECT_HEADER, BeerOrder.class);


        jmsTemplate.convertAndSend(JmsConfig.ALLOCATION_FAILURE_QUEUE, AllocationFailureEvent.builder()
                .beerOrderId(beerOrder.getId())
                .build());

        log.debug("Sent request to queue " + JmsConfig.ALLOCATE_ORDER_QUEUE + "for Beer Order Id: " + beerOrder.getId().toString());
    }
}
