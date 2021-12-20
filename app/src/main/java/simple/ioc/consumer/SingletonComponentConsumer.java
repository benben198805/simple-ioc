package simple.ioc.consumer;

import simple.ioc.component.Component;
import simple.ioc.component.SingletonComponent;

import javax.inject.Inject;

public class SingletonComponentConsumer implements Consumer {
    private Component component;

    @Inject
    public SingletonComponentConsumer(SingletonComponent component) {
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }
}
