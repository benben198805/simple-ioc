package simple.ioc.consumer;

import simple.ioc.component.Component;

import javax.inject.Inject;

public class ComponentConsumerWithTwoConstructors implements Consumer {
    private Component component;

    public ComponentConsumerWithTwoConstructors() {
    }

    @Inject
    public ComponentConsumerWithTwoConstructors(Component component) {
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return this.component;
    }
}
