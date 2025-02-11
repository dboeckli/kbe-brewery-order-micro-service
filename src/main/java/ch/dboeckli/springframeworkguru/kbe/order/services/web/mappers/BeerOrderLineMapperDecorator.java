package ch.dboeckli.springframeworkguru.kbe.order.services.web.mappers;

import ch.dboeckli.springframeworkguru.kbe.order.services.domain.BeerOrderLine;
import ch.dboeckli.springframeworkguru.kbe.order.services.dto.BeerDto;
import ch.dboeckli.springframeworkguru.kbe.order.services.dto.BeerOrderLineDto;
import ch.dboeckli.springframeworkguru.kbe.order.services.services.BeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;
import java.util.UUID;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {

    private BeerService beerService;
    private BeerOrderLineMapper beerOrderLineMapper;

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto orderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        Optional<BeerDto> beerDtoOptional = beerService.getBeerById(UUID.fromString(line.getBeerId()));

        beerDtoOptional.ifPresent(beerDto -> {
            orderLineDto.setBeerName(beerDto.getBeerName());
            orderLineDto.setBeerStyle(beerDto.getBeerName());
            orderLineDto.setUpc(beerDto.getUpc());
            orderLineDto.setPrice(beerDto.getPrice());
        });

        return orderLineDto;
    }
}
