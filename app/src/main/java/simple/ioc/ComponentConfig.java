package simple.ioc;

import javax.inject.Provider;

class ComponentConfig<T> {
    private Provider<T> provider;
    private String name;
    private String qualifierValue;

    public ComponentConfig(Provider<T> provider, String name, String qualifierValue) {
        this.provider = provider;
        this.name = name;
        this.qualifierValue = qualifierValue;
    }

    public Provider<T> getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public String getQualifierValue() {
        return qualifierValue;
    }
}
