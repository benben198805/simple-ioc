package simple.ioc;

import simple.ioc.exception.CircularDependenciesException;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Container<T> {
    private Map<ComponentKey, List<Provider<?>>> bindings = new HashMap<>();
    private Map<Class, Function<Provider<?>, Provider<?>>> scopeHandlers = new HashMap<>();

    public Container() {
        //bindScope();
    }

    public void bindScope(Class<T> clazz, Function<Provider<?>, Provider<?>> scopeHandler) {
        scopeHandlers.put(clazz, scopeHandler);
    }

    public <Api> void bind(Class<Api> clazz, final Api instance) {
        ComponentKey key = new ComponentKey(clazz, new HashSet<>());
        if (!bindings.containsKey(key))
            bindings.put(key, new ArrayList<>());
        bindings.get(key).add(() -> instance);
    }

    public <Api, Implementation> void bind(Class<Api> api, Class<Implementation> implementationClass) {
        ComponentKey key = new ComponentKey(api, getQualifier(implementationClass));
        if (!bindings.containsKey(key))
            bindings.put(key, new ArrayList<>());
        bindings.get(key).add(new ConstructorProvider<>(this, implementationClass));
    }

    public static Set<Annotation> getQualifier(Class clazz){
        return Stream.of(clazz.getAnnotations()).filter(it -> it.annotationType().isAnnotationPresent(Qualifier.class))
                .collect(Collectors.toSet());
    }
    
    public <Api> Api get(Class<Api> api, Annotation... annotations) {
        ComponentKey key = new ComponentKey(api, Arrays.stream(annotations).collect(Collectors.toSet()));
        return (Api) bindings.get(key).stream().findFirst().map(it -> it.get()).orElseThrow(RuntimeException::new);
    }

    static class ComponentKey {
        private Class api;
        private Set<Annotation> annotations;

        public ComponentKey(Class api, Set<Annotation> annotations) {
            this.api = api;
            this.annotations = annotations;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComponentKey that = (ComponentKey) o;
            return Objects.equals(api, that.api) && this.annotations.size() == that.annotations.size() && this.annotations.containsAll(that.annotations);
        }

        @Override
        public int hashCode() {
            return Objects.hash(api, annotations);
        }
    }
}
