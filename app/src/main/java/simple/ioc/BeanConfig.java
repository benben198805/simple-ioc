package simple.ioc;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

class BeanConfig<T> {
    private Provider<T> beanProvider;
    private String namedAnnotationValue;
    private String qualifierAnnotationValue;

    public BeanConfig(Provider<T> beanProvider, Class<T> clazz, T component) {

        String namedAnnotationValue = getNamedAnnotationValue(clazz, component);
        String qualifierAnnotationValue = getQualifierAnnotationValue(clazz, component);

        this.beanProvider = beanProvider;
        this.namedAnnotationValue = namedAnnotationValue;
        this.qualifierAnnotationValue = qualifierAnnotationValue;
    }

    private String getNamedAnnotationValue(Class<T> clazz, T component) {
        return Optional.ofNullable(component)
                       .filter(it -> !clazz.isInstance(it))
                       .map(it -> (Named) ((Class) it).getAnnotation(Named.class))
                       .map(Named::value).orElse(null);
    }

    private String getQualifierAnnotationValue(Class<T> clazz, T component) {
        if (clazz.isInstance(component)) {
            return null;
        }

        return Arrays.stream(((Class) component).getAnnotations())
                     .filter(annotation -> Arrays.stream(annotation.annotationType().getAnnotations())
                                                 .anyMatch(it -> it.annotationType().equals(Qualifier.class)))
                     .map(Annotation::toString).findAny().orElse(null);
    }

    public Object getBean() {
        return this.beanProvider.get();
    }

    public boolean getMatchedBeanConfig(String namedValue, String qualifierValue) {
        boolean matchNamedValue = Objects.nonNull(namedValue) && Objects.equals(this.namedAnnotationValue, namedValue);
        boolean matchQualifierValue = Objects.nonNull(qualifierValue) && Objects.equals(this.qualifierAnnotationValue, qualifierValue);
        return matchNamedValue || matchQualifierValue;
    }
}
