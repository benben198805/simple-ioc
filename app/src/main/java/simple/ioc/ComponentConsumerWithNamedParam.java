package simple.ioc;

import javax.inject.Inject;
import javax.inject.Named;

public class ComponentConsumerWithNamedParam implements Consumer {
    private ColorComponent redComponent;
    private ColorComponent blueComponent;

    @Inject
    public ComponentConsumerWithNamedParam(@Named("red") ColorComponent redComponent,
                                           @Named("blue") ColorComponent blueComponent) {
        this.redComponent = redComponent;
        this.blueComponent = blueComponent;
    }

    @Override
    public Component getComponent() {
        return null;
    }

    public ColorComponent getRedComponent() {
        return this.redComponent;
    }

    public ColorComponent getBlueComponent() {
        return this.blueComponent;
    }
}
