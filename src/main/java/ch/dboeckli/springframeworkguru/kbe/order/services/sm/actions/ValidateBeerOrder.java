package ch.dboeckli.springframeworkguru.kbe.order.services.sm.actions;

import ch.dboeckli.springframeworkguru.kbe.order.services.config.JmsConfig;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderEventEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderStatusEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.web.mappers.BeerOrderMapper;
import ch.guru.springframework.kbe.lib.events.ValidateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static ch.dboeckli.springframeworkguru.kbe.order.services.services.beerorder.BeerOrderManagerImpl.ORDER_OBJECT_HEADER;


@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateBeerOrder implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        BeerOrder beerOrder = stateContext.getStateMachine().getExtendedState()
            .get(ORDER_OBJECT_HEADER, BeerOrder.class);

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE, ValidateBeerOrderRequest
            .builder()
            .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
            .build());

        log.info("Sent request to queue" + JmsConfig.VALIDATE_ORDER_QUEUE + "for Beer Order Id: " + beerOrder.getId().toString());
    }
}
