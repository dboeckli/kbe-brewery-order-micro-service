package ch.dboeckli.springframeworkguru.kbe.order.services.web.mappers;

import ch.dboeckli.springframeworkguru.kbe.order.services.domain.Customer;
import ch.guru.springframework.kbe.lib.dto.CustomerDto;
import org.mapstruct.Mapper;

/**
 * Created by jt on 3/7/20.
 */
@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {
    CustomerDto customerToDto(Customer customer);

    Customer dtoToCustomer(Customer dto);
}
