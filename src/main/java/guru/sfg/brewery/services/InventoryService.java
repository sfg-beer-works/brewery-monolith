package guru.sfg.brewery.services;

import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.events.BrewBeerEvent;
import guru.sfg.brewery.repositories.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class InventoryService {

    private final BeerRepository beerRepository;
    private final ApplicationEventPublisher publisher;

    public InventoryService(BeerRepository beerRepository, ApplicationEventPublisher publisher) {
        this.beerRepository = beerRepository;
        this.publisher = publisher;
    }

    @Transactional
    @Scheduled(fixedRate = 5000) //run every 5 seconds
    public void checkInventory(){
        List<Beer> beers = beerRepository.findAll();

        beers.forEach(beer -> {
            AtomicInteger inventory_qoh = new AtomicInteger();

            beer.getBeerInventory().forEach(inv -> inventory_qoh.addAndGet(inv.getQuantityOnHand()));

            log.debug("Inv: " + beer.getBeerName() + " : QOH = " + inventory_qoh.get());

            if(beer.getMinOnHand() >= inventory_qoh.get() ) {
                //brew beer
                publisher.publishEvent(new BrewBeerEvent(beer));
            }
        });
     }
}
