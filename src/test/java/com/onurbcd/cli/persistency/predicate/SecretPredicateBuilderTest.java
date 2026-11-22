package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.persistency.entity.QSecret;
import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecretPredicateBuilderTest {

    private static final QSecret PATH = QSecret.secret;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("when search is null or blank, should not add criteria")
    void givenNullOrBlank_whenSearch_shouldNotAddCriteria(String search) {
        var builder = new SecretPredicateBuilder();

        var returned = builder.search(search);

        assertThat(returned).isSameAs(builder);
        assertThat(builder.build()).isEqualTo(new BooleanBuilder());
    }

    @Test
    @DisplayName("when search has text, should match name, description, link or username")
    void givenText_whenSearch_shouldMatchAnyTextColumn() {
        var builder = new SecretPredicateBuilder();

        var returned = builder.search("abc");

        assertThat(returned).isSameAs(builder);
        assertThat(builder.build()).isEqualTo(new BooleanBuilder().and(
                PATH.name.containsIgnoreCase("abc")
                        .or(PATH.description.containsIgnoreCase("abc"))
                        .or(PATH.link.containsIgnoreCase("abc"))
                        .or(PATH.username.containsIgnoreCase("abc"))));
    }

    @Test
    @DisplayName("active should filter by the active flag of the secret")
    void givenActive_whenBuild_shouldUseSecretActivePath() {
        var result = new SecretPredicateBuilder().active(true).build();

        assertThat(result).isEqualTo(new BooleanBuilder().and(PATH.active.eq(true)));
    }
}
