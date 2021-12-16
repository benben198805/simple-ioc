package simple.ioc;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Container<T> {
    private final Map<Class<T>, Provider<T>> existClasses = new HashMap<>();

    public void bind(Class<T> clazz, T component) {
        existClasses.put(clazz, processProvider(clazz, component));
    }

    private Provider<T> processProvider(Class<T> clazz, T component) {
        return () -> {
            try {
                if (clazz.isInstance(component)) {
                    return component;
                }

                Constructor constructor = Arrays.stream(((Class) component).getConstructors())
                                                .filter(it -> it.isAnnotationPresent(Inject.class))
                                                .findAny().orElseGet(() -> {
                            try {
                                return ((Class) component).getConstructor();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                Object[] params = Arrays.stream(constructor.getParameterTypes()).map(this::get).toArray();
                return (T) constructor.newInstance(params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public Object get(Class<T> clazz) {
        return existClasses.get(clazz).get();
    }
}
