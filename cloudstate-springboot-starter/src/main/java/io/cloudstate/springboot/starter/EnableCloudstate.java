package io.cloudstate.springboot.starter;

import io.cloudstate.springboot.starter.autoconfigure.CloudstateAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(CloudstateAutoConfiguration.class)
public @interface EnableCloudstate {}