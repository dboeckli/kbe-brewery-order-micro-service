package ch.dboeckli.springframeworkguru.kbe.order.services.sm.actions;

import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderEventEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2/25/20.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        log.error("Validation Failed");
    }
}
