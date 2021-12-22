package simple.ioc;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

class ConstructorProvider<T> implements Provider<T> {
    private Container container;
    private Class<T> implementation;

    public ConstructorProvider(Container container, Class<T> implementation) {
        this.container = container;
        this.implementation = implementation;
    }

    public T get() {
        Constructor constructor = getConstructor();
        Object[] paramInstances = getParamInstances(constructor);
        try {
            return (T) constructor.newInstance(paramInstances);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Constructor getConstructor() {
        return Arrays.stream(implementation.getConstructors())
                     .filter(it -> it.isAnnotationPresent(Inject.class))
                     .findAny().orElseGet(() -> {
                    try {
                        return implementation.getConstructor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private Object[] getParamInstances(Constructor constructor) {
        return Arrays.stream(constructor.getParameters())
                     .map(this::getParamInstance)
                     .toArray();
    }

    private Object getParamInstance(Parameter parameter) {
        return container.get(parameter.getType(), Stream.of(parameter.getAnnotations()).filter(it -> it.annotationType().isAnnotationPresent(Qualifier.class)).toArray(Annotation[]::new));
    }
}
