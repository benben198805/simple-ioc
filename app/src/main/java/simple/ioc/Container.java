package simple.ioc;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Container<T> {
    private final Map<Class<T>, Provider<T>> existClasses = new HashMap<>();
    private final Map<Class<T>, T> existInstances = new HashMap<>();

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
                Object[] params = this.getParams(constructor);
                return (T) constructor.newInstance(params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Object[] getParams(Constructor constructor) {
        Parameter[] parameters = constructor.getParameters();
        Class[] parameterTypes = constructor.getParameterTypes();
        ArrayList arrayList = new ArrayList();
        for (int index = 0; index < parameters.length; index++) {
            Object param = this.getComponentInstance(parameters[index], parameterTypes[index]);
            arrayList.add(param);
        }
        return arrayList.toArray();
    }

    private Object getComponentInstance(Parameter parameter, Class parameterType) {
        boolean hasSingleton = parameter.isAnnotationPresent(Singleton.class);
        boolean hasScope = Arrays.stream(parameter.getDeclaredAnnotations()).anyMatch(it -> it.annotationType().isAnnotationPresent(Scope.class));
        boolean isSingleton = hasSingleton || hasScope;

        if (isSingleton && existInstances.containsKey(parameterType)) {
            return existInstances.get(parameterType);
        }

        Object newInstance = this.get(parameterType);
        if (isSingleton) {
            existInstances.put(parameterType, (T) newInstance);
        }
        return newInstance;
    }

    public Object get(Class<T> clazz) {
        return existClasses.get(clazz).get();
    }
}
