package com.onurbcd.cli.config;

import jakarta.validation.Validator;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * Makes Hibernate validate entities with the Spring-managed validator, so constraint validators can receive
     * their dependencies by constructor injection.
     */
    @Bean
    public HibernatePropertiesCustomizer validationFactoryCustomizer(Validator validator) {
        return properties -> properties.put("jakarta.persistence.validation.factory", validator);
    }
}
