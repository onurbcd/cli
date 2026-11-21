package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class PrimeTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");

    private static Secret secret(UUID id) {
        var secret = new Secret();
        secret.setId(id);
        return secret;
    }

    @Nested
    @DisplayName("equals")
    class Equals {

        @Test
        @DisplayName("should be true for the same instance")
        void givenSameInstance_whenEquals_shouldBeTrue() {
            var category = new Category(ID);

            assertThat(category.equals(category)).isTrue();
        }

        @Test
        @DisplayName("should be true, in both directions, for the same type and id")
        void givenSameTypeAndId_whenEquals_shouldBeTrueBothWays() {
            var first = new Category(ID);
            var second = new Category(ID);

            assertThat(first.equals(second)).isTrue();
            assertThat(second.equals(first)).isTrue();
        }

        @Test
        @DisplayName("should be false for a different id")
        void givenDifferentId_whenEquals_shouldBeFalse() {
            assertThat(new Category(ID).equals(new Category(OTHER_ID))).isFalse();
        }

        @Test
        @DisplayName("should be false for null")
        void givenNull_whenEquals_shouldBeFalse() {
            assertThat(new Category(ID).equals(null)).isFalse();
        }

        @Test
        @DisplayName("should be false for another entity type with the same id")
        void givenOtherEntityTypeWithSameId_whenEquals_shouldBeFalse() {
            assertThat(new Category(ID).equals(secret(ID))).isFalse();
            assertThat(secret(ID).equals(new Category(ID))).isFalse();
        }

        @Test
        @DisplayName("should be false for an object that is not an entity")
        void givenNotAnEntity_whenEquals_shouldBeFalse() {
            Object notAnEntity = ID;

            assertThat(new Category(ID).equals(notAnEntity)).isFalse();
        }
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("should be the same for equal entities")
        void givenEqualEntities_whenHashCode_shouldMatch() {
            assertThat(new Category(ID).hashCode()).isEqualTo(new Category(ID).hashCode());
        }

        @Test
        @DisplayName("should differ for different ids")
        void givenDifferentIds_whenHashCode_shouldDiffer() {
            assertThat(new Category(ID).hashCode()).isNotEqualTo(new Category(OTHER_ID).hashCode());
        }

        @Test
        @DisplayName("should not fail when the id is not set yet")
        void givenNoId_whenHashCode_shouldNotFail() {
            assertThat(new Category((UUID) null).hashCode()).isEqualTo(new Category((UUID) null).hashCode());
        }
    }
}
