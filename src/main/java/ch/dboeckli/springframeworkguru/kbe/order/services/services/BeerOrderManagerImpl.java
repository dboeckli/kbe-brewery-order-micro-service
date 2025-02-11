package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderEventEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderStatusEnum;
import ch.dboeckli.springframeworkguru.kbe.order.services.dto.BeerOrderDto;
import ch.dboeckli.springframeworkguru.kbe.order.services.repositories.BeerOrderRepository;
import ch.dboeckli.springframeworkguru.kbe.order.services.sm.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jt on 2019-09-08.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID";
    public static final String ORDER_OBJECT_HEADER = "BEER_ORDER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        log.debug("New Order: {}", beerOrder);

        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder savedOrder = beerOrderRepository.saveAndFlush(beerOrder);

        //send validation event
        sendBeerOrderEvent(savedOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedOrder;
    }

    @Transactional
    @Override
    public void beerOrderPassedValidation(UUID beerOrderId) {

        log.debug("Order Passed Validation:" + beerOrderId);

        awaitForStatus(beerOrderId, BeerOrderStatusEnum.PENDING_VALIDATION);

        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATED);

            BeerOrder validatedOrder = beerOrderRepository.findById(beerOrderId).get();

            sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
        }, () -> log.error("Order Not Found. Id: " + beerOrderId));
    }

    public void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 20) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }

            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if (beerOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Order Found");
                } else {
                    log.debug("Order Status Not Equal. Expected: " + statusEnum.name() + " Found: " + beerOrder.getOrderStatus().name());
                }
            }, () -> log.debug("Order Id Not Found"));

            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(200);
                } catch (Exception e) {
                    log.info("Did not found order {} with status {}. Retry", beerOrderId, statusEnum);
                }
            }
        }
    }

    @Transactional
    @Override
    public void beerOrderFailedValidation(UUID beerOrderId) {

        awaitForStatus(beerOrderId, BeerOrderStatusEnum.PENDING_VALIDATION);

        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED), () -> log.error("Order Not Found. Id: " + beerOrderId));
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {

        log.debug("Allocation Passed: " + beerOrderDto.toString());

        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);
            updateAllocatedQty(beerOrderDto);
        }, () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);
            updateAllocatedQty(beerOrderDto);
        }, () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        BeerOrder allocatedOrder = beerOrderRepository.getReferenceById(beerOrderDto.getId());

        allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
            }
        }));

        beerOrderRepository.saveAndFlush(allocatedOrder);
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED), () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));
    }

    @Override
    public void pickupBeerOrder(UUID beerOrderId) {
        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEER_ORDER_PICKED_UP), () -> log.error("Order Not Found. Id: " + beerOrderId));
    }

    @Override
    public void cancelOrder(UUID beerOrderId) {
        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER), () -> log.error("Order Not Found. Id: " + beerOrderId));
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);

        Message<BeerOrderEventEnum> beerOrderEventMessage = MessageBuilder.withPayload(event)
            .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
            .build();

        stateMachine.sendEvent(beerOrderEventMessage);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {

        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
            .doWithAllRegions(stateMachineAccess -> {
                stateMachineAccess.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                stateMachineAccess.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null,
                    null, null));
            });

        stateMachine.getExtendedState().getVariables().put(ORDER_OBJECT_HEADER, beerOrder);

        stateMachine.start();

        return stateMachine;
    }
}
