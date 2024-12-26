package ch.dboeckli.springframeworkguru.kbe.order.services.services;

import ch.dboeckli.springframeworkguru.kbe.order.services.domain.Customer;
import ch.dboeckli.springframeworkguru.kbe.order.services.dto.CustomerPagedList;
import ch.dboeckli.springframeworkguru.kbe.order.services.repositories.CustomerRepository;
import ch.dboeckli.springframeworkguru.kbe.order.services.web.mappers.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Created by jt on 3/7/20.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerPagedList listCustomers(Pageable pageable) {

        Page<Customer> customerPage = customerRepository.findAll(pageable);

        return new CustomerPagedList(customerPage
                        .stream()
                        .map(customerMapper::customerToDto)
                        .collect(Collectors.toList()),
                    PageRequest.of(customerPage.getPageable().getPageNumber(),
                        customerPage.getPageable().getPageSize()),
                        customerPage.getTotalElements());
    }
}
