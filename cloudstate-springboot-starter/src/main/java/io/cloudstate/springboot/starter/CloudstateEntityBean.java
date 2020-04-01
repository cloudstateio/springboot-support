package io.cloudstate.springboot.starter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface CloudstateEntityBean {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}