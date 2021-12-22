package simple.ioc.consumer;

import simple.ioc.component.Component;

import javax.inject.Inject;

public class CircularDependenciesBConsumer implements Consumer {

    private CircularDependenciesAConsumer consumer;

    @Inject
    public CircularDependenciesBConsumer(CircularDependenciesAConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public Component getComponent() {
        return null;
    }

    public CircularDependenciesAConsumer getConsumer() {
        return consumer;
    }
}
