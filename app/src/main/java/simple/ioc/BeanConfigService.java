package simple.ioc;

import com.google.common.collect.ImmutableList;

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
    private final Map<String, T> existInstances = new HashMap<>();
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

                Constructor constructor = getConstructor((Class) component);
                Object[] paramInstances = this.getParamInstances(constructor);
                return (T) constructor.newInstance(paramInstances);
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
        boolean hasSingleton = parameter.isAnnotationPresent(Singleton.class);
        boolean hasScope = Arrays.stream(parameter.getDeclaredAnnotations())
                                 .anyMatch(it -> it.annotationType().isAnnotationPresent(Scope.class));
        boolean isSingleton = hasSingleton || hasScope;

        String namedAnnotationValue = Optional.of(parameter)
                                              .map(it -> it.getAnnotation(Named.class))
                                              .map(Named::value).orElse(null);

        String qualifierAnnotationValue = Arrays
                .stream(parameter.getAnnotations())
                .filter(annotation -> Arrays.stream(annotation.annotationType().getAnnotations())
                                            .anyMatch(it -> it.annotationType().equals(Qualifier.class)))
                .findAny().map(Annotation::toString)
                .orElse(null);

        String instanceKey = String.join(":", ImmutableList.of(parameter.getType().toString(),
                String.valueOf(namedAnnotationValue),
                String.valueOf(qualifierAnnotationValue)));

        if (isSingleton && existInstances.containsKey(instanceKey)) {
            return existInstances.get(instanceKey);
        }

        Object paramInstance = Objects.nonNull(namedAnnotationValue) || Objects.nonNull(qualifierAnnotationValue) ?
                this.container.get(parameter.getType(), namedAnnotationValue, qualifierAnnotationValue) :
                this.container.get(parameter.getType());

        if (isSingleton) {
            existInstances.put(instanceKey, (T) paramInstance);
        }
        return paramInstance;
    }
}
