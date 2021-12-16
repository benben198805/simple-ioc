package simple.ioc;

import javax.inject.Provider;

class ComponentConfig<T> {
    private Provider<T> provider;
    private String name;

    public ComponentConfig(Provider<T> provider, String name) {
        this.provider = provider;
        this.name = name;
    }

    public Provider<T> getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }
}
