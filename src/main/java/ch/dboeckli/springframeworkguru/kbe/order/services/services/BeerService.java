package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.dto.BeerPagedList;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<BeerDto> getBeerById(UUID uuid);

    Optional<BeerDto> getBeerByUpc(String upc);

    Optional<BeerPagedList> getListofBeers();
}
