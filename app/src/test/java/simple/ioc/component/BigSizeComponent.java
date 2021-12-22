package simple.ioc.component;

import simple.ioc.annotation.SizeQualifier;

@SizeQualifier(value = "big")
public class BigSizeComponent implements SizeComponent {
    public BigSizeComponent() {
    }

    @Override
    public String getSize() {
        return "big";
    }
}
