package simple.ioc;

import java.util.HashMap;
import java.util.Map;

public class Container<T> {
    private final Map<Object, Object> existClasses = new HashMap<>();

    public void bind(Class<T> clazz, T component) {
        existClasses.put(clazz, component);
    }

    public Object get(Class<T> clazz) {
        return existClasses.get(clazz);
    }
}
