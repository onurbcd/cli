package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.persistency.entity.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private DocumentRepository repository;

    @Test
    void givenExistingId_whenDeleteUsingId_shouldRemoveOnlyThatRow() {
        var target = fixtures.document("a.pdf");
        var other = fixtures.document("b.pdf");

        var deleted = repository.deleteUsingId(target);

        assertThat(deleted).isEqualTo(1);
        assertThat(jdbc.queryForList("select id from document", UUID.class)).containsExactly(other);
    }

    @Test
    void givenUnknownId_whenDeleteUsingId_shouldReturnZeroAndKeepRows() {
        var existing = fixtures.document("a.pdf");

        var deleted = repository.deleteUsingId(UUID.randomUUID());

        assertThat(deleted).isZero();
        assertThat(jdbc.queryForList("select id from document", UUID.class)).containsExactly(existing);
    }

    @Test
    void givenNewEntity_whenSave_shouldGenerateIdAndPersist() {
        var document = new Document();
        document.setName("a.pdf");
        document.setPath("/docs/a.pdf");
        document.setMimeType("application/pdf");
        document.setSize(10L);
        document.setHash("0123456789abcdef0123456789abcdef");

        var saved = repository.save(document);
        em.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(jdbc.queryForObject("select name from document where id = ?", String.class, saved.getId()))
                .isEqualTo("a.pdf");
    }
}
