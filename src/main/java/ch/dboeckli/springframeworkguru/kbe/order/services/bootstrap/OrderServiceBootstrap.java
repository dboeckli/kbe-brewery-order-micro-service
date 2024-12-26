package ch.dboeckli.springframeworkguru.kbe.order.services.bootstrap;

import ch.dboeckli.springframeworkguru.kbe.order.services.domain.Customer;
import ch.dboeckli.springframeworkguru.kbe.order.services.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 2019-09-29.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderServiceBootstrap implements CommandLineRunner {

    public static final String CUSTOMER_NAME = "Bird Dog Brewing";


    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        Optional<Customer> customerOptional = customerRepository.findByCustomerName(CUSTOMER_NAME);

        if (customerOptional.isEmpty()) {
            //create if not found
            Customer savedCustomer = customerRepository.save(Customer.builder()
                    .customerName(CUSTOMER_NAME)
                    .apiKey(UUID.randomUUID())
                    .build());

            log.info("##################################################################");
            log.info("# Saved Customer Id: " + savedCustomer.getId()  + "#");
            log.info("##################################################################");
        } else {
            log.info("##################################################################");
            log.info("# Found Customer Id: " + customerOptional.get().getId() + "#");
            log.info("##################################################################");
        }

    }
}
