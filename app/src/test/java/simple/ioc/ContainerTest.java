package simple.ioc;

import org.junit.jupiter.api.Test;
import simple.ioc.component.BigSizeComponent;
import simple.ioc.component.BlueComponent;
import simple.ioc.component.ColorComponent;
import simple.ioc.component.Component;
import simple.ioc.component.ComponentWithDefaultConstructor;
import simple.ioc.component.CustomComponent;
import simple.ioc.component.ScopeComponent;
import simple.ioc.component.RedComponent;
import simple.ioc.component.SingletonComponent;
import simple.ioc.component.SizeComponent;
import simple.ioc.component.SmallSizeComponent;
import simple.ioc.consumer.ComponentConsumer;
import simple.ioc.consumer.ComponentConsumerWithNamedParam;
import simple.ioc.consumer.ComponentConsumerWithQualifierParam;
import simple.ioc.consumer.ComponentConsumerWithTwoConstructors;
import simple.ioc.consumer.Consumer;
import simple.ioc.consumer.OtherComponentConsumer;
import simple.ioc.consumer.ScopeComponentConsumer;
import simple.ioc.consumer.SingletonComponentConsumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ContainerTest {
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
        Container container = new Container();

        container.bind(SingletonComponent.class, SingletonComponent.class);
        container.bind(SingletonComponentConsumer.class, SingletonComponentConsumer.class);

        SingletonComponentConsumer componentConsumer = (SingletonComponentConsumer) container.get(SingletonComponentConsumer.class);
        SingletonComponentConsumer otherComponentConsumer = (SingletonComponentConsumer) container.get(SingletonComponentConsumer.class);

        SingletonComponent componentA = (SingletonComponent) componentConsumer.getComponent();
        SingletonComponent componentB = (SingletonComponent) otherComponentConsumer.getComponent();
        assertSame(componentA, componentB);
        componentA.setValue("abc");
        assertSame("abc", componentB.getValue());
    }

    @Test
    public void should_inject_to_scope_component() {
        Container container = new Container();

        container.bind(ScopeComponent.class, ScopeComponent.class);
        container.bind(ScopeComponentConsumer.class, ScopeComponentConsumer.class);

        ScopeComponentConsumer componentConsumer = (ScopeComponentConsumer) container.get(ScopeComponentConsumer.class);
        ScopeComponentConsumer otherComponentConsumer = (ScopeComponentConsumer) container.get(ScopeComponentConsumer.class);

        ScopeComponent componentA = (ScopeComponent) componentConsumer.getComponent();
        ScopeComponent componentB = (ScopeComponent) otherComponentConsumer.getComponent();
        assertSame(componentA, componentB);
        componentA.setValue("abc");
        assertSame("abc", componentB.getValue());
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
}
