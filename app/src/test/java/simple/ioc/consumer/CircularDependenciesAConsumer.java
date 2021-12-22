package simple.ioc.consumer;

import simple.ioc.component.Component;

import javax.inject.Inject;

public class CircularDependenciesAConsumer implements Consumer {

    private CircularDependenciesBConsumer consumer;

    @Inject
    public CircularDependenciesAConsumer(CircularDependenciesBConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public Component getComponent() {
        return null;
    }

    public CircularDependenciesBConsumer getConsumer() {
        return consumer;
    }
}
