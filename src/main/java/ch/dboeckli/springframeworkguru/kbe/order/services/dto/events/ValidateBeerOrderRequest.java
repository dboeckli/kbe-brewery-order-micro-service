package ch.dboeckli.springframeworkguru.kbe.order.services.dto.events;

import ch.dboeckli.springframeworkguru.kbe.order.services.dto.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jt on 2019-09-08.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateBeerOrderRequest {

    private BeerOrderDto beerOrder;
}
