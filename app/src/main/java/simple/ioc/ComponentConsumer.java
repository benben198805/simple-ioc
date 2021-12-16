package simple.ioc;

import javax.inject.Inject;

public class ComponentConsumer implements Consumer {
    private Component component;

    @Inject
    public ComponentConsumer(Component component) {
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return this.component;
    }
}
