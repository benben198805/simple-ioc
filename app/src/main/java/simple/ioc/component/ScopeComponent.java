package simple.ioc.component;

import simple.ioc.annotation.CustomScope;

@CustomScope
public class ScopeComponent implements Component {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
