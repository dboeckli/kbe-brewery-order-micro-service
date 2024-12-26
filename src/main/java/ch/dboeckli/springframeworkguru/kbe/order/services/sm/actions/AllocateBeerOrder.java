package ch.dboeckli.springframeworkguru.kbe.order.services.sm.actions;

import ch.dboeckli.springframeworkguru.kbe.order.services.config.JmsConfig;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderEventEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderStatusEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.dto.events.AllocateBeerOrderRequest;
import ch.dboeckli.springframeworkguru.kbe.order.services.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static ch.dboeckli.springframeworkguru.kbe.order.services.services.BeerOrderManagerImpl.ORDER_OBJECT_HEADER;

/**
 * Created by jt on 2019-09-08.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateBeerOrder implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;


    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

        log.debug("Sending Allocation Request...");

        BeerOrder beerOrder = context.getStateMachine().getExtendedState()
                .get(ORDER_OBJECT_HEADER, BeerOrder.class);

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE, AllocateBeerOrderRequest
                .builder()
                .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                .build());

        log.debug("Sent request to queue" + JmsConfig.ALLOCATE_ORDER_QUEUE + "for Beer Order Id: " + beerOrder.getId().toString());
    }
}
