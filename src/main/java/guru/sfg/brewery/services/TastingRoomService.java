package guru.sfg.brewery.services;

import guru.sfg.brewery.bootstrap.DefaultBreweryLoader;
import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.domain.Customer;
import guru.sfg.brewery.repositories.BeerOrderRepository;
import guru.sfg.brewery.repositories.BeerRepository;
import guru.sfg.brewery.repositories.CustomerRepository;
import guru.sfg.brewery.web.model.BeerOrderDto;
import guru.sfg.brewery.web.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;

    private final BeerRepository beerRepository;
    private final BeerOrderRepository beerOrderRepository;

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService, BeerRepository beerRepository,
                              BeerOrderRepository beerOrderRepository) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;
        this.beerRepository = beerRepository;
        this.beerOrderRepository = beerOrderRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 2000) //run every 2 seconds
    public void placeTastingRoomOrder(){

        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(DefaultBreweryLoader.TASTING_ROOM);

        if (customerList.size() == 1){ //should be just one
            doPlaceOrder(customerList.get(0));
        } else {
            log.error("Too many or too few tasting room customers found");
        }
    }

    private void doPlaceOrder(Customer customer) {
        Beer beerToOrder = getRandomBeer();

        BeerOrderLineDto beerOrderLine = BeerOrderLineDto.builder()
                .beerId(beerToOrder.getId())
                .orderQuantity(new Random().nextInt(6)) //todo externalize value to property
                .build();

        List<BeerOrderLineDto> beerOrderLineSet = new ArrayList<>();
        beerOrderLineSet.add(beerOrderLine);

        BeerOrderDto beerOrder = BeerOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
               // .orderStatusCallbackUrl("http://localhost:8080/beerorder") //todo update
                .beerOrderLines(beerOrderLineSet)
                .build();

        BeerOrderDto savedOrder = beerOrderService.placeOrder(customer.getId(), beerOrder);

        log.debug("Saved Tasting Room Order: " + savedOrder.getId());
    }

    private Beer getRandomBeer() {
        List<Beer> beers = beerRepository.findAll();

        return beers.get(new Random().nextInt(beers.size() -0));
    }
}
