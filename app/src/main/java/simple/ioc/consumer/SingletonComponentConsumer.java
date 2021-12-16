package simple.ioc.consumer;

import simple.ioc.component.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

public class SingletonComponentConsumer implements Consumer {
    private Component component;

    @Inject
    public SingletonComponentConsumer(@Singleton Component component) {
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }
}
