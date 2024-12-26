package ch.dboeckli.springframeworkguru.kbe.order.services.domain;

/**
 * Created by jt on 2019-09-07.
 */
public enum  BeerOrderEventEnum {
    CANCEL_ORDER, VALIDATE_ORDER, VALIDATION_PASSED, VALIDATION_FAILED,
    ALLOCATE_ORDER, ALLOCATION_SUCCESS, ALLOCATION_FAILED, ALLOCATION_NO_INVENTORY,
    BEER_ORDER_PICKED_UP
}
