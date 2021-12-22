package simple.ioc.component;

import javax.inject.Singleton;

@Singleton
public class SingletonComponent implements Component {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
