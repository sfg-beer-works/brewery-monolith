package guru.sfg.brewery.events;

import guru.sfg.brewery.domain.Beer;
import org.springframework.context.ApplicationEvent;

public class NewInventoryEvent extends ApplicationEvent {

    public NewInventoryEvent(Beer source) {
        super(source);
    }

    public Beer getBeer(){
        return (Beer) this.source;
    }
}
