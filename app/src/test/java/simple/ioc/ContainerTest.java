package simple.ioc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        assertSame(component, ((Consumer)container.get(Consumer.class)).getComponent());
    }
}
