package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.guru.springframework.kbe.lib.dto.BeerDto;
import ch.guru.springframework.kbe.lib.dto.BeerPagedList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
public class BeerServiceImpl implements BeerService {

    public static final String LIST_BEER_PATH_V1 = "/api/v1/beer";
    public static final String BEER_PATH_V1 = "/api/v1/beer/";
    public static final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";

    private final RestTemplate restTemplate;

    private final String beerServiceHost;

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder, 
                           @Value("${sfg.brewery.beer-service-host}") String beerServiceHost) {
        this.restTemplate = restTemplateBuilder.build();
        this.beerServiceHost = beerServiceHost;
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID uuid){
        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + BEER_PATH_V1 + uuid.toString(), BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String upc) {
        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH_V1 + upc, BeerDto.class));
    }

    @Override
    public Optional<BeerPagedList> getListofBeers() {
        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + LIST_BEER_PATH_V1, BeerPagedList.class));
    }
}
