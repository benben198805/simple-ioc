package simple.ioc;

import javax.inject.Provider;

class BeanConfig<T> {
    private Provider<T> beanProvider;
    private String namedValue;
    private String qualifierValue;

    public BeanConfig(Provider<T> beanProvider, String name, String qualifierValue) {
        this.beanProvider = beanProvider;
        this.namedValue = name;
        this.qualifierValue = qualifierValue;
    }

    public Provider<T> getBeanProvider() {
        return beanProvider;
    }

    public String getNamedValue() {
        return namedValue;
    }

    public String getQualifierValue() {
        return qualifierValue;
    }
}
