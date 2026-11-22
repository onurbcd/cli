package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.persistency.entity.Secret;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecretRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private SecretRepository repository;

    @Nested
    @DisplayName("deleteUsingId")
    class DeleteUsingId {

        @Test
        void givenExistingId_whenDelete_shouldRemoveOnlyThatRow() {
            var target = fixtures.secret("Bank", true);
            var other = fixtures.secret("Mail", true);

            var deleted = repository.deleteUsingId(target);

            assertThat(deleted).isEqualTo(1);
            assertThat(ids()).containsExactly(other);
        }

        @Test
        void givenUnknownId_whenDelete_shouldReturnZeroAndKeepRows() {
            var existing = fixtures.secret("Bank", true);

            var deleted = repository.deleteUsingId(UUID.randomUUID());

            assertThat(deleted).isZero();
            assertThat(ids()).containsExactly(existing);
        }
    }

    @Nested
    @DisplayName("updateActive")
    class UpdateActive {

        @Test
        void givenExistingId_whenUpdateActive_shouldChangeOnlyThatRow() {
            var target = fixtures.secret("Bank", true);
            var other = fixtures.secret("Mail", true);

            var updated = repository.updateActive(target, false);

            assertThat(updated).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(active(other)).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            var existing = fixtures.secret("Bank", true);

            var updated = repository.updateActive(UUID.randomUUID(), false);

            assertThat(updated).isZero();
            assertThat(active(existing)).isTrue();
        }
    }

    private List<UUID> ids() {
        return jdbc.queryForList("select id from secret", UUID.class);
    }

    private Boolean active(UUID id) {
        return jdbc.queryForObject("select active from secret where id = ?", Boolean.class, id);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("a new entity without id should get a generated UUID and be persisted")
        void givenNewEntity_whenSave_shouldGenerateIdAndPersist() {
            var entity = Secret.builder().name("Bank").active(true).description("Online banking")
                    .link("https://example.com").username("user").password("pwd").build();

            var saved = repository.save(entity);
            clear();

            assertThat(saved.getId()).isNotNull();
            assertThat(jdbc.queryForObject("select username from secret where id = ?", String.class, saved.getId()))
                    .isEqualTo("user");
            assertThat(jdbc.queryForObject("select created_date from secret where id = ?", Object.class, saved.getId()))
                    .as("auditing fills created_date").isNotNull();
        }
    }
}
