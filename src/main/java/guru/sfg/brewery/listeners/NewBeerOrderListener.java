package guru.sfg.brewery.listeners;

import guru.sfg.brewery.domain.BeerInventory;
import guru.sfg.brewery.domain.BeerOrder;
import guru.sfg.brewery.domain.BeerOrderLine;
import guru.sfg.brewery.domain.OrderStatusEnum;
import guru.sfg.brewery.events.NewBeerOrderEvent;
import guru.sfg.brewery.repositories.BeerInventoryRepository;
import guru.sfg.brewery.repositories.BeerOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class NewBeerOrderListener {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerInventoryRepository beerInventoryRepository;

    public NewBeerOrderListener(BeerOrderRepository beerOrderRepository, BeerInventoryRepository beerInventoryRepository) {
        this.beerOrderRepository = beerOrderRepository;
        this.beerInventoryRepository = beerInventoryRepository;
    }

    @Async
    @EventListener
    @Transactional
    public synchronized void listen(NewBeerOrderEvent event){ //needed sychronized to prevent errors
        log.debug("Allocating Order: " + event.getBeerOrder().getCustomerRef());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        BeerOrder beerOrder = beerOrderRepository.findOneById(event.getBeerOrder().getId());

        if (beerOrder == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //event firing before save is complete
            beerOrder = beerOrderRepository.findOneById(event.getBeerOrder().getId());
        }

        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            if ((beerOrderLine.getOrderQuantity() - beerOrderLine.getQuantityAllocated()) > 0) {
                allocateBeerOrderLine(beerOrderLine);
            }
            totalOrdered.set(totalOrdered.get() + beerOrderLine.getOrderQuantity());
            totalAllocated.set(totalAllocated.get() + beerOrderLine.getQuantityAllocated());
        });

        if(totalOrdered.get() == totalAllocated.get()){
            log.debug("Order Completely Allocated: " + beerOrder.getCustomerRef());
            beerOrder.setOrderStatus(OrderStatusEnum.READY);
        }

        beerOrderRepository.saveAndFlush(beerOrder);
    }

    private void allocateBeerOrderLine(BeerOrderLine beerOrderLine) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByBeer(beerOrderLine.getBeer());

        beerInventoryList.forEach(beerInventory -> {
            int inventory = (beerInventory.getQuantityOnHand() == null) ? 0 : beerInventory.getQuantityOnHand();
            int orderQty = (beerOrderLine.getOrderQuantity() == null) ? 0 : beerOrderLine.getOrderQuantity() ;
            int allocatedQty = (beerOrderLine.getQuantityAllocated() == null) ? 0 : beerOrderLine.getQuantityAllocated();
            int qtyToAllocate = orderQty - allocatedQty;

            if(inventory >= qtyToAllocate){ // full allocation
                inventory = inventory - qtyToAllocate;
                beerOrderLine.setQuantityAllocated(orderQty);
                beerInventory.setQuantityOnHand(inventory);
            } else if (inventory > 0) { //partial allocation
                beerOrderLine.setQuantityAllocated(allocatedQty + inventory);
                beerInventory.setQuantityOnHand(0);
            }
        });

        beerInventoryRepository.saveAll(beerInventoryList);

        beerInventoryList.stream()
                .filter(beerInventory -> beerInventory.getQuantityOnHand() == 0)
                .forEach(beerInventory -> beerInventoryRepository.delete(beerInventory));

    }
}
