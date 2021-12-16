package simple.ioc.consumer;

import simple.ioc.annotation.CustomScope;
import simple.ioc.component.Component;

import javax.inject.Inject;

public class ScopeComponentConsumer implements Consumer {
    private Component component;

    @Inject
    public ScopeComponentConsumer(@CustomScope Component component) {
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }
}
