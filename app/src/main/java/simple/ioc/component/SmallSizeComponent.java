package simple.ioc.component;

import simple.ioc.annotation.SizeQualifier;

import javax.inject.Named;

@Named
@SizeQualifier(value = "small")
public class SmallSizeComponent implements SizeComponent {
    public SmallSizeComponent() {
    }

    @Override
    public String getSize() {
        return "small";
    }
}
