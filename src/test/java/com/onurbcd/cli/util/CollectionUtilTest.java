package com.onurbcd.cli.util;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class CollectionUtilTest {

    private record Holder(String text, Integer number) {
    }

    private static final class Empty {
    }

    private record WithStatic(String text) {
        static final String CONSTANT = "constant";
    }

    private static Stream<Arguments> emptyCollections() {
        return Stream.of(
                Arguments.of((Collection<Object>) null),
                Arguments.of(List.of()),
                Arguments.of(Arrays.asList(null, null)),
                Arguments.of(List.of(new Holder(null, null))),
                Arguments.of(List.of(new Holder(null, null), new Holder(null, null))),
                Arguments.of(Arrays.asList(new Holder(null, null), null)),
                Arguments.of(Arrays.asList(null, new Holder(null, null))),
                Arguments.of(List.of(new Empty())));
    }

    private static Stream<Arguments> nonEmptyCollections() {
        return Stream.of(
                Arguments.of(List.of(new Holder("a", null))),
                Arguments.of(List.of(new Holder(null, 1))),
                Arguments.of(List.of(new Holder(null, null), new Holder("a", 1))),
                Arguments.of(List.of(new Holder("a", 1))),
                Arguments.of(Arrays.asList(new Holder("a", null), null)),
                Arguments.of(Arrays.asList(null, new Holder("a", null))),
                Arguments.of(Arrays.asList(null, "text")),
                Arguments.of(List.of(new WithStatic("a"))),
                Arguments.of(List.of("text")),
                Arguments.of(List.of("")),
                Arguments.of(List.of(1)),
                Arguments.of(List.of(UUID.randomUUID())),
                Arguments.of(List.of(LocalDate.of(2025, 1, 1))));
    }

    @Nested
    @DisplayName("getValue")
    class GetValue {

        @Test
        @DisplayName("should map the first element that matches the predicate")
        void givenMatches_shouldMapFirst() {
            var result = CollectionUtil.getValue(List.of("a", "bb", "cc"), s -> s.length() == 2, String::toUpperCase);

            assertThat(result).isEqualTo("BB");
        }

        @Test
        @DisplayName("should return null when nothing matches")
        void givenNoMatch_shouldReturnNull() {
            var result = CollectionUtil.getValue(List.of("a", "b"), s -> s.length() == 2, String::toUpperCase);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return null for an empty collection")
        void givenEmpty_shouldReturnNull() {
            var result = CollectionUtil.getValue(List.<String>of(), s -> true, String::toUpperCase);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should not change the collection")
        void shouldNotMutateCollection() {
            var collection = new ArrayList<>(List.of("a", "bb"));

            CollectionUtil.getValue(collection, s -> s.length() == 2, String::toUpperCase);

            assertThat(collection).containsExactly("a", "bb");
        }
    }

    @Nested
    @DisplayName("isEmpty / isNotEmpty")
    class Emptiness {

        @ParameterizedTest
        @MethodSource("com.onurbcd.cli.util.CollectionUtilTest#emptyCollections")
        @DisplayName("null, empty, all-null or all-fields-null collections are empty")
        void givenEmptyLike_shouldBeEmpty(Collection<Object> collection) {
            assertThat(CollectionUtil.isEmpty(collection)).isTrue();
            assertThat(CollectionUtil.isNotEmpty(collection)).isFalse();
        }

        @Test
        @DisplayName("a non-null static field is not part of the instance state")
        void givenOnlyStaticState_shouldBeEmpty() {
            assertThat(WithStatic.CONSTANT).isNotNull();

            assertThat(CollectionUtil.isEmpty(List.of(new WithStatic(null)))).isTrue();
        }

        @Test
        @DisplayName("when a field cannot be read, the element is treated as empty")
        void givenUnreadableField_shouldBeEmpty() {
            var collection = List.of(new Holder("a", 1));

            try (var fieldUtils = mockStatic(FieldUtils.class)) {
                fieldUtils.when(() -> FieldUtils.getAllFields(Holder.class))
                        .thenReturn(Holder.class.getDeclaredFields());
                fieldUtils.when(() -> FieldUtils.readField(any(Field.class), any(Object.class), eq(true)))
                        .thenThrow(new IllegalAccessException("denied"));

                assertThat(CollectionUtil.isEmpty(collection)).isTrue();
            }
        }

        @ParameterizedTest
        @MethodSource("com.onurbcd.cli.util.CollectionUtilTest#nonEmptyCollections")
        @DisplayName("collections with at least one populated element are not empty")
        void givenPopulated_shouldNotBeEmpty(Collection<Object> collection) {
            assertThat(CollectionUtil.isEmpty(collection)).isFalse();
            assertThat(CollectionUtil.isNotEmpty(collection)).isTrue();
        }
    }
}
