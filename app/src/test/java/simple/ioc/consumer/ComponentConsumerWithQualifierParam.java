package simple.ioc.consumer;

import simple.ioc.component.SizeComponent;
import simple.ioc.annotation.SizeQualifier;
import simple.ioc.component.Component;

import javax.inject.Inject;

public class ComponentConsumerWithQualifierParam implements Consumer {
    private SizeComponent bigSizeComponent;
    private SizeComponent smallSizeComponent;

    @Inject
    public ComponentConsumerWithQualifierParam(@SizeQualifier(value = "big") SizeComponent bigSizeComponent,
                                               @SizeQualifier(value = "small") SizeComponent smallSizeComponent) {
        this.bigSizeComponent = bigSizeComponent;
        this.smallSizeComponent = smallSizeComponent;
    }

    @Override
    public Component getComponent() {
        return null;
    }

    public SizeComponent getBigSizeComponent() {
        return bigSizeComponent;
    }

    public SizeComponent getSmallSizeComponent() {
        return smallSizeComponent;
    }
}
