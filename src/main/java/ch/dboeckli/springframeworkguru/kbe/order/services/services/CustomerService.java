package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.dto.CustomerPagedList;
import org.springframework.data.domain.Pageable;

/**
 * Created by jt on 3/7/20.
 */
public interface CustomerService {

    CustomerPagedList listCustomers(Pageable pageable);

}