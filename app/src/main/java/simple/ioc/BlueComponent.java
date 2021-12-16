package simple.ioc;

import javax.inject.Named;

@Named("blue")
public class BlueComponent implements ColorComponent {
    public BlueComponent() {
    }

    @Override
    public String getColor() {
        return "blue";
    }
}
