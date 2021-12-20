package simple.ioc;

import simple.ioc.exception.CircularDependenciesException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Container<T> {
    private final Map<Class<T>, List<BeanConfig>> existClasses = new HashMap<>();
    private final BeanConfigService beanConfigService;

    public Container() {
        this.beanConfigService = new BeanConfigService(this);
    }

    public void bind(Class<T> clazz, T component) {
        BeanConfig beanConfig = this.beanConfigService.generateBeanConfig(clazz, component);

        existClasses.compute(clazz, (key, beanConfigs) ->
                Optional.ofNullable(beanConfigs).map(it -> {
                    it.add(beanConfig);
                    return it;
                }).orElse(new ArrayList<>() {{
                    add(beanConfig);
                }}));
    }

    public Object get(Class clazz, String namedValue, String qualifierValue) {
        BeanConfig beanConfig = existClasses.get(clazz).stream()
                                            .filter(it -> it.filterBeanConfigByAnnotationValue(namedValue, qualifierValue))
                                            .findAny().orElseThrow(RuntimeException::new);
        return getBean(beanConfig);
    }

    public Object get(Class<T> clazz) {
        BeanConfig beanConfig = existClasses.get(clazz).stream()
                                            .findAny().orElseThrow(RuntimeException::new);
        return getBean(beanConfig);
    }

    private Object getBean(BeanConfig beanConfig) {
        try {
            return beanConfig.getBean();
        } catch (StackOverflowError error) {
            throw new CircularDependenciesException("error");
        }
    }
}
