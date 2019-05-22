package guru.sfg.brewery.listeners;

import guru.sfg.brewery.domain.Beer;
import guru.sfg.brewery.domain.BeerInventory;
import guru.sfg.brewery.events.BrewBeerEvent;
import guru.sfg.brewery.events.NewInventoryEvent;
import guru.sfg.brewery.repositories.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Slf4j
@Component
public class BrewBeerListener {

    private final BeerRepository beerRepository;
    private final ApplicationEventPublisher publisher;

    public BrewBeerListener(BeerRepository beerRepository, ApplicationEventPublisher publisher) {
        this.beerRepository = beerRepository;
        this.publisher = publisher;
    }

    @Async
    @EventListener
    @Transactional
    public void listen(BrewBeerEvent event){
        log.debug("Brewing Beer for " + event.getBeer().getBeerName() );

        Beer beer = beerRepository.getOne(event.getBeer().getId());

        BeerInventory beerInventory = BeerInventory.builder()
                                        .beer(beer)
                                        .quantityOnHand(beer.getQuantityToBrew())
                                        .build();

        beer.getBeerInventory().add(beerInventory);

        Beer savedBeer = beerRepository.save(beer);

        log.debug("Brewed some beer! " + savedBeer.getBeerName());
        publisher.publishEvent(new NewInventoryEvent(beer));
    }
}
