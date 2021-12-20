package simple.ioc;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class BeanConfigService<T> {
    private final Map<T, T> existInstances = new HashMap<>();
    private Container container;

    public BeanConfigService(Container container) {
        this.container = container;
    }

    public BeanConfig generateBeanConfig(Class<T> clazz, T component) {
        return new BeanConfig(getBeanProvider(clazz, component), clazz, component);
    }

    private Provider<T> getBeanProvider(Class<T> clazz, T component) {
        return () -> {
            try {
                if (clazz.isInstance(component)) {
                    return component;
                }

                boolean isSingleton = isSingleton(component);
                if (isSingleton && existInstances.containsKey(component)) {
                    return existInstances.get(component);
                }

                Constructor constructor = getConstructor((Class) component);
                Object[] paramInstances = this.getParamInstances(constructor);
                T instance = (T) constructor.newInstance(paramInstances);

                if (isSingleton && !existInstances.containsKey(component)) {
                    existInstances.put(component, instance);
                }

                return instance;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Constructor getConstructor(Class component) {
        return Arrays.stream(component.getConstructors())
                     .filter(it -> it.isAnnotationPresent(Inject.class))
                     .findAny().orElseGet(() -> {
                    try {
                        return component.getConstructor();
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
        String namedAnnotationValue = Optional.of(parameter)
                                              .map(it -> it.getAnnotation(Named.class))
                                              .map(Named::value).orElse(null);

        String qualifierAnnotationValue = Arrays
                .stream(parameter.getAnnotations())
                .filter(annotation -> Arrays.stream(annotation.annotationType().getAnnotations())
                                            .anyMatch(it -> it.annotationType().equals(Qualifier.class)))
                .findAny().map(Annotation::toString)
                .orElse(null);

        return Objects.nonNull(namedAnnotationValue) || Objects.nonNull(qualifierAnnotationValue) ?
                this.container.get(parameter.getType(), namedAnnotationValue, qualifierAnnotationValue) :
                this.container.get(parameter.getType());
    }

    private boolean isSingleton(T component) {
        boolean hasSingleton = ((Class) component).isAnnotationPresent(Singleton.class);
        System.out.println(component);
        System.out.println(hasSingleton);
        boolean hasScope = Arrays.stream(((Class) component).getDeclaredAnnotations())
                                 .anyMatch(it -> it.annotationType().isAnnotationPresent(Scope.class));
        System.out.println(hasScope);
        boolean isSingleton = hasSingleton || hasScope;
        return isSingleton;
    }
}
