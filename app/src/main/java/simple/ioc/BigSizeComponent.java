package simple.ioc;

import javax.inject.Named;

@Named
@SizeQualifier(value = "big")
public class BigSizeComponent implements SizeComponent {
    public BigSizeComponent() {
    }

    @Override
    public String getSize() {
        return "big";
    }
}
