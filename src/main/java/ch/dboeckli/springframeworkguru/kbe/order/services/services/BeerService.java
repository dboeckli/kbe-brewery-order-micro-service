package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.dto.BeerDto;
import ch.dboeckli.springframeworkguru.kbe.order.services.dto.BeerPagedList;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<BeerDto> getBeerById(UUID uuid);

    Optional<BeerDto> getBeerByUpc(String upc);

    Optional<BeerPagedList> getListofBeers();
}
