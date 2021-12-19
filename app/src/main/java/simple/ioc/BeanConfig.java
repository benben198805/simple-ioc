package simple.ioc;

import javax.inject.Provider;
import java.util.Objects;

class BeanConfig<T> {
    private Provider<T> beanProvider;
    private String namedValue;
    private String qualifierValue;

    public BeanConfig(Provider<T> beanProvider, String name, String qualifierValue) {
        this.beanProvider = beanProvider;
        this.namedValue = name;
        this.qualifierValue = qualifierValue;
    }

    public Object getBean() {
        return this.beanProvider.get();
    }

    public boolean getMatchedBeanConfig(String namedValue, String qualifierValue) {
        boolean matchNamedValue = Objects.nonNull(namedValue) && Objects.equals(this.namedValue, namedValue);
        boolean matchQualifierValue = Objects.nonNull(qualifierValue) && Objects.equals(this.qualifierValue, qualifierValue);
        return matchNamedValue || matchQualifierValue;
    }
}
