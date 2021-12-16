package simple.ioc;

import javax.inject.Inject;

public class OtherComponentConsumer implements Consumer {
    private Component component;

    @Inject
    public OtherComponentConsumer(Component component) {
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }
}
