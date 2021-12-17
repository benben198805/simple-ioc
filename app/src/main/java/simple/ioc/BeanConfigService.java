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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class BeanConfigService<T> {
    private final Map<String, T> existInstances = new HashMap<>();
    private Container container;

    public <T> BeanConfigService(Container container) {
        this.container = container;
    }

    public BeanConfig generateBeanConfig(Class<T> clazz, T component) {
        String namedValue = null;
        String qualifierValue = null;

        if (existNamedAnnotation(clazz, component)) {
            namedValue = getNamedValue(component);
        }

        if (existQualifierValue(clazz, component)) {
            qualifierValue = getQualifierValue(component);
        }

        return new BeanConfig(processProvider(clazz, component), namedValue, qualifierValue);
    }

    private String getNamedValue(T component) {
        return ((Named) ((Class) component).getAnnotation(Named.class)).value();
    }

    private boolean existNamedAnnotation(Class<T> clazz, T component) {
        if (clazz.isInstance(component)) {
            return false;
        }
        return ((Class) component).isAnnotationPresent(Named.class);
    }

    private String getQualifierValue(T component) {
        String QualifierValue = null;
        for (Annotation annotation : ((Class) component).getAnnotations()) {
            boolean anyMatch = Arrays.stream(annotation.annotationType().getAnnotations()).anyMatch(it -> it.annotationType().equals(Qualifier.class));
            if (anyMatch) {
                QualifierValue = annotation.toString();
            }
        }

        return QualifierValue;
    }

    private boolean existQualifierValue(Class<T> clazz, T component) {
        if (clazz.isInstance(component)) {
            return false;
        }

        boolean isQualifier = false;
        for (Annotation annotation : ((Class) component).getAnnotations()) {
            boolean anyMatch = Arrays.stream(annotation.annotationType().getAnnotations()).anyMatch(it -> it.annotationType().equals(Qualifier.class));
            if (anyMatch) {
                isQualifier = true;
            }
        }

        return isQualifier;
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

        String key = parameterType.toString();
        boolean isNamed = parameter.isAnnotationPresent(Named.class);
        String namedValue = null;

        if (isNamed) {
            namedValue = parameter.getAnnotation(Named.class).value();
            key = "named:" + namedValue;
        }


        boolean isQualifier = false;
        String qualifierValue = null;
        for (Annotation annotation : parameter.getAnnotations()) {
            boolean anyMatch = Arrays.stream(annotation.annotationType().getAnnotations()).anyMatch(it -> it.annotationType().equals(Qualifier.class));
            if (anyMatch) {
                isQualifier = true;
                qualifierValue = annotation.toString();
                key = "qualifier:" + namedValue;
            }
        }

        if (isSingleton && existInstances.containsKey(key)) {
            return existInstances.get(key);
        }

        Object newInstance = isNamed || isQualifier ? this.container.get(parameterType, namedValue, qualifierValue) : this.container.get(parameterType);

        if (isSingleton) {
            existInstances.put(key, (T) newInstance);
        }
        return newInstance;
    }
}
