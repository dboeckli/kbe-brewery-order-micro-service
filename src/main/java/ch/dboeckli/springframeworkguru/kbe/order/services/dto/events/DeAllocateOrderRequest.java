package ch.dboeckli.springframeworkguru.kbe.order.services.dto.events;

import ch.dboeckli.springframeworkguru.kbe.order.services.dto.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeAllocateOrderRequest {
    private BeerOrderDto beerOrder;
}
