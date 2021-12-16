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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Container<T> {
    private final Map<Class<T>, List<ComponentConfig>> existClasses = new HashMap<>();
    private final Map<String, T> existInstances = new HashMap<>();

    public void bind(Class<T> clazz, T component) {
        if (existClasses.containsKey(clazz)) {
            List<ComponentConfig> providerConfigs = existClasses.get(clazz);
            providerConfigs.add(getComponentConfig(clazz, component));
            existClasses.put(clazz, providerConfigs);
        } else {
            existClasses.put(clazz, new ArrayList<>() {{
                add(getComponentConfig(clazz, component));
            }});
        }
    }

    private ComponentConfig getComponentConfig(Class<T> clazz, T component) {
        String namedValue = null;
        String qualifierValue = null;

        if (existNamedAnnotation(clazz, component)) {
            namedValue = getNamedValue(component);
        }

        if (existQualifierValue(clazz, component)) {
            qualifierValue = getQualifierValue(component);
        }

        return new ComponentConfig(processProvider(clazz, component), namedValue, qualifierValue);
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
            key += namedValue + ";";
        }


        boolean isQualifier = false;
        String QualifierValue = null;
        for (Annotation annotation : parameter.getAnnotations()) {
            boolean anyMatch = Arrays.stream(annotation.annotationType().getAnnotations()).anyMatch(it -> it.annotationType().equals(Qualifier.class));
            if (anyMatch) {
                isQualifier = true;
                QualifierValue = annotation.toString();
                key += QualifierValue + ";";
            }
        }

        if (isSingleton && existInstances.containsKey(key)) {
            return existInstances.get(key);
        }

        Object newInstance = isNamed || isQualifier ? this.get(parameterType, namedValue, QualifierValue) : this.get(parameterType);

        if (isSingleton) {
            existInstances.put(key, (T) newInstance);
        }
        return newInstance;
    }

    private Object get(Class clazz, String namedValue, String qualifierValue) {
        Optional<ComponentConfig> componentConfig = existClasses.get(clazz).stream()
                                                                .filter(it -> (Objects.nonNull(namedValue) && Objects.equals(it.getName(), namedValue))
                                                                        || (Objects.nonNull(qualifierValue) && Objects.equals(it.getQualifierValue(), qualifierValue)))
                                                                .findAny();
        return componentConfig.get().getProvider().get();
    }

    public Object get(Class<T> clazz) {
        ComponentConfig componentConfig = existClasses.get(clazz).stream().filter(it -> Objects.isNull(it.getName())).collect(Collectors.toList()).stream().findAny().orElseThrow(RuntimeException::new);
        return componentConfig.getProvider().get();
    }

}
