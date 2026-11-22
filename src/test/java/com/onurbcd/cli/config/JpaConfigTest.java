package com.onurbcd.cli.config;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class JpaConfigTest {

    @Mock
    private Validator validator;

    private final JpaConfig config = new JpaConfig();

    @Test
    @DisplayName("should enable JPA auditing")
    void whenInspectingClass_shouldBeAnnotatedWithEnableJpaAuditing() {
        assertThat(JpaConfig.class.isAnnotationPresent(EnableJpaAuditing.class)).isTrue();
    }

    @Nested
    @DisplayName("localValidatorFactoryBean")
    class LocalValidatorFactoryBeanMethod {

        @Test
        @DisplayName("should return a new validator factory bean on each call")
        void whenCalled_shouldReturnNewFactoryBean() {
            var first = config.localValidatorFactoryBean();
            var second = config.localValidatorFactoryBean();

            assertThat(first).isInstanceOf(LocalValidatorFactoryBean.class).isNotSameAs(second);
        }
    }

    @Nested
    @DisplayName("validationFactoryCustomizer")
    class ValidationFactoryCustomizer {

        @Test
        @DisplayName("should register the given validator as the JPA validation factory")
        void givenValidator_whenCustomize_shouldPutValidatorInProperties() {
            var properties = new HashMap<String, Object>();
            properties.put("other", "value");

            config.validationFactoryCustomizer(validator).customize(properties);

            assertThat(properties)
                    .hasSize(2)
                    .containsEntry("other", "value")
                    .containsEntry("jakarta.persistence.validation.factory", validator);
            verifyNoInteractions(validator);
        }
    }
}
