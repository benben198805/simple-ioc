package simple.ioc;

import javax.inject.Provider;

class BeanConfig<T> {
    private Provider<T> beanProvider;
    private String name;
    private String qualifierValue;

    public BeanConfig(Provider<T> beanProvider, String name, String qualifierValue) {
        this.beanProvider = beanProvider;
        this.name = name;
        this.qualifierValue = qualifierValue;
    }

    public Provider<T> getBeanProvider() {
        return beanProvider;
    }

    public String getName() {
        return name;
    }

    public String getQualifierValue() {
        return qualifierValue;
    }
}
