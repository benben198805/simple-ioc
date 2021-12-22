package simple.ioc;

import org.junit.jupiter.api.Test;
import simple.ioc.component.BigSizeComponent;
import simple.ioc.component.BlueComponent;
import simple.ioc.component.ColorComponent;
import simple.ioc.component.Component;
import simple.ioc.component.ComponentWithDefaultConstructor;
import simple.ioc.component.CustomComponent;
import simple.ioc.component.RedComponent;
import simple.ioc.component.SingletonComponent;
import simple.ioc.component.SizeComponent;
import simple.ioc.component.SmallSizeComponent;
import simple.ioc.consumer.CircularDependenciesAConsumer;
import simple.ioc.consumer.CircularDependenciesBConsumer;
import simple.ioc.consumer.ComponentConsumer;
import simple.ioc.consumer.ComponentConsumerWithNamedParam;
import simple.ioc.consumer.ComponentConsumerWithQualifierParam;
import simple.ioc.consumer.ComponentConsumerWithTwoConstructors;
import simple.ioc.consumer.Consumer;
import simple.ioc.consumer.OtherComponentConsumer;
import simple.ioc.exception.CircularDependenciesException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContainerTest {

    private static final String TEST_VALUE = "test_value_abc";


    // req -> scenario -> FT @Disbale, FT -> unit -> test

//    @Test
//    public void should_happy() {
//        //....
//    }
//
//    @Test
//    public void should_sad() {
//
//    }


    @Test
    public void should_bind_class_to_special_instance() {
        // given
        Container container = new Container();
        Component component = new Component() {
        };

        // when
        container.bind(Component.class, component);

        // then
        assertSame(component, container.get(Component.class));
    }

    @Test
    public void should_inject_dependencies_by_constructor() {
        // given
        Container container = new Container();
        Component component = new Component() {
        };

        // when
        container.bind(Component.class, component);
        container.bind(Consumer.class, ComponentConsumer.class);

        // then
        assertSame(component, ((Consumer) container.get(Consumer.class)).getComponent());
    }

    @Test
    public void should_bind_type_to_specific_instance_with_default_constructor() {
        Container container = new Container();

        container.bind(ComponentWithDefaultConstructor.class, ComponentWithDefaultConstructor.class);

        assertNotNull(container.get(ComponentWithDefaultConstructor.class));
    }

    @Test
    public void should_inject_to_annotated_constructor() {
        Container container = new Container();

        Component component = new Component() {
        };

        container.bind(Component.class, component);
        container.bind(Consumer.class, ComponentConsumerWithTwoConstructors.class);

        assertSame(component, ((Consumer) container.get(Consumer.class)).getComponent());
    }

    @Test
    public void should_inject_to_component_instance_everytime() {
        Container container = new Container();

        container.bind(Component.class, CustomComponent.class);
        container.bind(ComponentConsumer.class, ComponentConsumer.class);
        container.bind(OtherComponentConsumer.class, OtherComponentConsumer.class);

        ComponentConsumer componentConsumer = (ComponentConsumer) container.get(ComponentConsumer.class);
        ComponentConsumer otherComponentConsumer = (ComponentConsumer) container.get(ComponentConsumer.class);

        assertNotSame(componentConsumer.getComponent(), otherComponentConsumer.getComponent());
    }

    @Test
    public void should_inject_to_singleton_component() {
        // given
        Container container = new Container();
        Component component = new Component() {
        };

        // when
        container.bind(Component.class, component);
        container.bind(Consumer.class, ComponentConsumer.class);

        // then
        assertSame(component, ((Consumer) container.get(Consumer.class)).getComponent());
    }

    @Test
    public void should_not_re_create_singleton_component() {
        Container container = new Container();

        container.bind(SingletonComponent.class, SingletonComponent.class);

        assertSame(container.get(SingletonComponent.class), container.get(SingletonComponent.class));
    }

    @Test
    public void should_inject_to_component_with_name() {
        Container container = new Container();

        container.bind(ColorComponent.class, BlueComponent.class);
        container.bind(ColorComponent.class, RedComponent.class);
        container.bind(Consumer.class, ComponentConsumerWithNamedParam.class);

        ComponentConsumerWithNamedParam consumer = (ComponentConsumerWithNamedParam) container.get(Consumer.class);
        assertSame("red", consumer.getRedComponent().getColor());
        assertSame("blue", consumer.getBlueComponent().getColor());
    }

    @Test
    public void should_inject_to_component_with_qualifier() {
        Container container = new Container();

        container.bind(SizeComponent.class, BigSizeComponent.class);
        container.bind(SizeComponent.class, SmallSizeComponent.class);
        container.bind(Consumer.class, ComponentConsumerWithQualifierParam.class);

        ComponentConsumerWithQualifierParam consumer = (ComponentConsumerWithQualifierParam) container.get(Consumer.class);
        assertSame("big", consumer.getBigSizeComponent().getSize());
        assertSame("small", consumer.getSmallSizeComponent().getSize());
    }

    @Test
    public void should_throw_exception_when_circular_dependencies() {
        Container container = new Container();

        container.bind(ProviderConsumer.class, ProviderConsumer.class);
        container.bind(Component.class, Component.class);

        assertThrows(CircularDependenciesException.class, () -> {
            ProviderConsumer consumer =
                    (ProviderConsumer) container.get(ProviderConsumer.class);
            consumer.getComponent();
        });
    }

    @Test
    public void should_throw_exception_when_easy_circular_dependencies() {
        Container container = new Container();

        container.bind(CircularDependenciesAConsumer.class, CircularDependenciesAConsumer.class);
        container.bind(CircularDependenciesBConsumer.class, CircularDependenciesBConsumer.class);

        assertThrows(CircularDependenciesException.class, () -> {
            CircularDependenciesAConsumer consumer =
                    (CircularDependenciesAConsumer) container.get(CircularDependenciesAConsumer.class);
            consumer.getConsumer();
        });
    }

    @Test
    public void should_customize_scope() {
        int poolSize = 5;
        Container container = new Container();
        container.bindScope(Pooled.class, it -> new PoolScope(poolSize, (Provider<Object>) it));

        container.bind(Component.class, PoolComponent.class);

        Set<Component> instances = new HashSet<>();
        for (int i = 0; i < poolSize * 2; i++)
            instances.add((Component) container.get(Component.class));

        assertEquals(poolSize, instances.size());
    }

    @Test
    public void should_inject_provider() {
        Container container = new Container();
        Component component = new Component() {
        };

        // when
        container.bind(Component.class, component);
        container.bind(ProviderConsumer.class, ProviderConsumer.class);

        assertSame(component, ((ProviderConsumer)container.get(ProviderConsumer.class)).get());



//        1.重构代码
//        2.实现循环依赖
//        3.实现Optional，可选的
//        4.List
//        5.Map

        // Provider<?>
        // Optional<?>
        // List<Color> colors

        //Map<Named, PaymentGateway> payments

        //@Named("wechat")
        //class xxx implements PaymentGateway

        //@Named("alipay")
        //class xxx implements PaymentGateway

    }

    static class OptionalConsumer implements Consumer {
        Component component;
        public OptionalConsumer(Optional<Component> provider) {
            this.component = provider.get();
        }

        public Component getComponent() {
            return component;
        }
    }

    static class ProviderConsumer implements Consumer {
        Component component;
        public ProviderConsumer(Provider<Component> provider) {
            this.component = provider.get();
        }

        public Component getComponent() {
            return component;
        }
    }

//    @Test @Disabled
//    public void should_custom_scope() {
//        int poolSize = 5;
//        Container container = new Container();
//        BeanConfigService beanConfigService = new BeanConfigService(container);
//        beanConfigService.bindScope(Pooled.class, (it) -> new PoolScope(poolSize, (Provider) it));
//
//        BeanConfig config = beanConfigService.generateBeanConfig(Component.class, PoolComponent.class);
//
//        Set<Component> instances = new HashSet<>();
//        for (int i = 0; i < poolSize * 2; i++)
//            instances.add((Component) config.getBean());
//
//        assertEquals(poolSize, instances.size());
//    }

//    @Test @Disabled
//    public void should_singleton() {
//        Container container = new Container();
//        BeanConfigService beanConfigService = new BeanConfigService(container);
//        beanConfigService.bindScope(Singleton.class, (it) -> new SingletonScope((Provider) it));
//
//        BeanConfig config = beanConfigService.generateBeanConfig(Component.class, SingletonComponent.class);
//
//        assertSame(config.getBean(), config.getBean());
//    }
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Scope
@interface Pooled {
}

@Pooled
class PoolComponent implements Component {
    @Inject
    public PoolComponent() {
    }
}

class SingletonScope implements Provider<Object> {
    private Provider provider;
    private Object singleton;

    public SingletonScope(Provider provider) {
        this.provider = provider;
    }

    public Object get() {
        if (singleton == null) {
            singleton = provider.get();
        }
        return singleton;
    }
}

class PoolScope implements Provider<Object> {
    private int size;
    private int current = 0;
    private Provider<Object> provider;
    private List<Object> pool;

    public PoolScope(int size, Provider provider) {
        this.size = size;
        this.provider = provider;
        this.pool = new ArrayList<>();
    }

    public Object get() {
        if (pool.size() < size) pool.add(provider.get());
        current = (current + 1) % pool.size();
        return pool.get(current);
    }
}
