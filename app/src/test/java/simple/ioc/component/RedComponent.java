package simple.ioc.component;

import javax.inject.Named;

@Named("red")
public class RedComponent implements ColorComponent {
    public RedComponent() {
    }

    @Override
    public String getColor() {
        return "red";
    }
}
