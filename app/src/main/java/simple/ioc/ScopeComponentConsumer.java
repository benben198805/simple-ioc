package simple.ioc;

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
