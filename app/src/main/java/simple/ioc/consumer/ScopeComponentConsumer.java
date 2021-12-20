package simple.ioc.consumer;

import simple.ioc.component.Component;
import simple.ioc.component.ScopeComponent;

import javax.inject.Inject;

public class ScopeComponentConsumer implements Consumer {
    private Component component;

    @Inject
    public ScopeComponentConsumer(ScopeComponent component) {
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }
}
