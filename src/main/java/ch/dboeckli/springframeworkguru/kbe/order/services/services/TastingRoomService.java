package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.bootstrap.OrderServiceBootstrap;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrder;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderLine;
import ch.dboeckli.springframeworkguru.kbe.order.services.domain.Customer;
import ch.dboeckli.springframeworkguru.kbe.order.services.repositories.CustomerRepository;
import ch.dboeckli.springframeworkguru.kbe.order.services.services.beer.BeerService;
import ch.dboeckli.springframeworkguru.kbe.order.services.services.beerorder.BeerOrderManager;
import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.dto.BeerPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jt on 2019-09-29.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TastingRoomService {
    private final BeerOrderManager beerOrderManager;
    private final CustomerRepository customerRepository;
    private final BeerService beerService;

    @Scheduled(fixedRateString = "${sfg.tasting.room.rate}")
    public void createTastingRoomOrder() {

        getRandomBeer().ifPresent(beerId -> {

            Customer customer = customerRepository.findByCustomerName(OrderServiceBootstrap.CUSTOMER_NAME).orElseThrow();

            BeerOrder beerOrder = BeerOrder.builder().customer(customer).build();

            BeerOrderLine line = BeerOrderLine.builder()
                .beerId(beerId.toString())
                .beerOrder(beerOrder)
                .orderQuantity(new Random().nextInt(5) + 1) //zero based
                .build();

            Set<BeerOrderLine> lines = new HashSet<>(1);
            lines.add(line);

            beerOrder.setBeerOrderLines(lines);

            beerOrderManager.newBeerOrder(beerOrder);
        });
    }

    private Optional<UUID> getRandomBeer() {

        Optional<BeerPagedList> listOptional = beerService.getListofBeers();

        if (listOptional.isPresent()) {
            BeerPagedList beerPagedList = listOptional.get();

            beerPagedList.getContent();
            if (!beerPagedList.getContent().isEmpty()) {
                List<BeerDto> dtoList = beerPagedList.getContent();

                int k = new Random().nextInt(dtoList.size());

                return Optional.of(dtoList.get(k).getId());
            }
        }

        log.debug("Failed to get list of beers");

        return Optional.empty();

    }
}
