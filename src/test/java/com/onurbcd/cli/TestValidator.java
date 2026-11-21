package com.onurbcd.cli;

import com.onurbcd.cli.config.property.AdminProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestValidator {

    /**
     * Real validator wired like production: constraint validators are created by Spring, so they receive
     * {@link AdminProperties} (minimum year 2000) and the {@link TestClock#DEFAULT} clock by constructor injection.
     */
    public static Validator create() {
        var properties = new AdminProperties();
        properties.setMinYear((short) 2000);
        var context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("adminProperties", properties);
        context.getBeanFactory().registerSingleton("clock", TestClock.DEFAULT);
        context.refresh();
        var factory = new LocalValidatorFactoryBean();
        factory.setApplicationContext(context);
        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * Distinct violation messages produced by validating {@code target}.
     */
    public static Set<String> messages(Validator validator, Object target) {
        return validator.validate(target).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
