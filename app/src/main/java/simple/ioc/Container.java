package simple.ioc;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        if (existNamedAnnotation(clazz, component)) {
            return new ComponentConfig(processProvider(clazz, component), getNamedValue(component));
        } else {
            return new ComponentConfig(processProvider(clazz, component), null);
        }
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

                                Constructor temp = ((Class) component).getConstructor();
                                System.out.println("this.is.temp");
                                System.out.println(temp);
                                return temp;
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
        String namedValue = "";
        if (isNamed) {
            namedValue = parameter.getAnnotation(Named.class).value();
            key += namedValue;
        }

        if (isSingleton && existInstances.containsKey(key)) {
            return existInstances.get(key);
        }

        Object newInstance = isNamed ? this.get(parameterType, namedValue) : this.get(parameterType);

        if (isSingleton) {
            existInstances.put(key, (T) newInstance);
        }
        return newInstance;
    }

    private Object get(Class clazz, String namedValue) {
        ComponentConfig componentConfig = existClasses.get(clazz).stream()
                                                      .filter(it -> Objects.nonNull(it.name))
                                                      .filter(it -> it.name.equals(namedValue))
                                                      .findAny().orElseThrow(RuntimeException::new);
        return componentConfig.provider.get();
    }

    public Object get(Class<T> clazz) {
        ComponentConfig componentConfig = existClasses.get(clazz).stream().filter(it -> Objects.isNull(it.name)).collect(Collectors.toList()).stream().findAny().orElseThrow(RuntimeException::new);
        return componentConfig.provider.get();
    }

    private class ComponentConfig {
        private Provider<T> provider;
        private String name;

        public ComponentConfig(Provider<T> provider, String name) {
            this.provider = provider;
            this.name = name;
        }
    }
}
