package com.onurbcd.cli.factory;

import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.persistency.entity.QIncomeSource;
import com.onurbcd.cli.persistency.predicate.BasePredicateBuilder;
import com.onurbcd.cli.persistency.predicate.IncomeSourcePredicateBuilder;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PredicateBuilderFactoryTest {

    public static class ThrowingConstructorBuilder extends BasePredicateBuilder {

        public ThrowingConstructorBuilder() {
            super(QIncomeSource.incomeSource.name, QIncomeSource.incomeSource.active);
            throw new IllegalStateException("boom");
        }
    }

    public static class PrivateConstructorBuilder extends BasePredicateBuilder {

        private PrivateConstructorBuilder() {
            super(QIncomeSource.incomeSource.name, QIncomeSource.incomeSource.active);
        }
    }

    public static class ArgsConstructorBuilder extends BasePredicateBuilder {

        public ArgsConstructorBuilder(StringPath name, BooleanPath active) {
            super(name, active);
        }
    }

    @Nested
    @DisplayName("init")
    class Init {

        @Test
        @DisplayName("with a public no-args builder, should return a new instance of that builder")
        void givenBuilderClass_shouldReturnNewInstance() {
            var first = PredicateBuilderFactory.init(IncomeSourcePredicateBuilder.class);
            var second = PredicateBuilderFactory.init(IncomeSourcePredicateBuilder.class);

            assertThat(first).isExactlyInstanceOf(IncomeSourcePredicateBuilder.class);
            assertThat(second).isExactlyInstanceOf(IncomeSourcePredicateBuilder.class).isNotSameAs(first);
            assertThat(first.search("abc").active(true).build())
                    .hasToString("containsIc(incomeSource.name,abc) && incomeSource.active = true");
        }

        @Test
        @DisplayName("without a no-args constructor, should translate NoSuchMethodException")
        void givenNoNoArgsConstructor_shouldThrowInternalServerError() {
            assertThatThrownBy(() -> PredicateBuilderFactory.init(ArgsConstructorBuilder.class))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
                        assertThat(e.getCause()).isInstanceOf(NoSuchMethodException.class);
                    });
        }

        @Test
        @DisplayName("with a non-public constructor, should translate the reflection failure")
        void givenPrivateConstructor_shouldThrowInternalServerError() {
            assertThatThrownBy(() -> PredicateBuilderFactory.init(PrivateConstructorBuilder.class))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
                        assertThat(e.getCause()).isInstanceOf(NoSuchMethodException.class);
                    });
        }

        @Test
        @DisplayName("when the constructor throws, should translate InvocationTargetException")
        void givenThrowingConstructor_shouldThrowInternalServerError() {
            assertThatThrownBy(() -> PredicateBuilderFactory.init(ThrowingConstructorBuilder.class))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
                        assertThat(e.getCause()).isInstanceOf(InvocationTargetException.class)
                                .hasRootCauseInstanceOf(IllegalStateException.class);
                    });
        }

        @Test
        @DisplayName("with an abstract class, should translate InstantiationException")
        void givenAbstractClass_shouldThrowInternalServerError() {
            assertThatThrownBy(() -> PredicateBuilderFactory.init(AbstractBuilder.class))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
                        assertThat(e.getCause()).isInstanceOf(InstantiationException.class);
                    });
        }
    }

    public abstract static class AbstractBuilder extends BasePredicateBuilder {

        public AbstractBuilder() {
            super(QIncomeSource.incomeSource.name, QIncomeSource.incomeSource.active);
        }
    }
}
