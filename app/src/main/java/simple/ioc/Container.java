package simple.ioc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Container<T> {
    private final Map<Class<T>, List<BeanConfig>> existClasses = new HashMap<>();
    private final BeanConfigService componentConfigService;

    public Container() {
        this.componentConfigService = new BeanConfigService(this);
    }

    public void bind(Class<T> clazz, T component) {
        BeanConfig beanConfig = this.componentConfigService.generateBeanConfig(clazz, component);
        existClasses.compute(clazz, (key, value) -> {
            if (Objects.isNull(value)) {
                return new ArrayList<>() {{
                    add(beanConfig);
                }};
            } else {
                value.add(beanConfig);
                return value;
            }
        });
    }

    public Object get(Class clazz, String namedValue, String qualifierValue) {
        BeanConfig beanConfig = existClasses.get(clazz).stream()
                                            .filter(it -> matchBeanConfigByNamedAndQualifier(it, namedValue, qualifierValue))
                                            .findAny().orElseThrow(RuntimeException::new);
        return beanConfig.getBean();
    }

    public Object get(Class<T> clazz) {
        BeanConfig beanConfig = existClasses.get(clazz).stream().filter(it -> Objects.isNull(it.getNamedValue()))
                                            .findAny().orElseThrow(RuntimeException::new);
        return beanConfig.getBean();
    }

    private boolean matchBeanConfigByNamedAndQualifier(BeanConfig beanConfig, String namedValue, String qualifierValue) {
        boolean matchNamedValue = Objects.nonNull(namedValue) && Objects.equals(beanConfig.getNamedValue(), namedValue);
        boolean matchQualifierValue = Objects.nonNull(qualifierValue) && Objects.equals(beanConfig.getQualifierValue(), qualifierValue);
        return matchNamedValue || matchQualifierValue;
    }
}
