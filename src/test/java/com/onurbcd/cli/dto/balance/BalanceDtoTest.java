package com.onurbcd.cli.dto.balance;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestFactory.createBalanceDto;
import static com.onurbcd.cli.TestFactory.createDocumentDto;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceDtoTest {

    @Test
    @DisplayName("is a PrimeDto and exposes every field set through the setters")
    void givenFactoryBalance_shouldExposeFields() {
        var balance = createBalanceDto();

        assertThat(balance).isInstanceOf(PrimeDto.class);
        assertThat(balance.getSequence()).isEqualTo((short) 3);
        assertThat(balance.getDayId()).isNull();
        assertThat(balance.getSourceName()).isEqualTo("source");
        assertThat(balance.getCategoryName()).isEqualTo("category");
        assertThat(balance.getCode()).isEqualTo("CODE");
        assertThat(balance.getDescription()).isEqualTo("description");

        balance.setDayId(7);
        assertThat(balance.getDayId()).isEqualTo(7);
    }

    @Nested
    @DisplayName("getDocumentsIds")
    class GetDocumentsIds {

        @Test
        @DisplayName("without documents, should return null")
        void givenNullDocuments_shouldReturnNull() {
            var balance = new BalanceDto();

            assertThat(balance.getDocumentsIds()).isNull();
        }

        @Test
        @DisplayName("with an empty set, should return an empty set")
        void givenEmptyDocuments_shouldReturnEmptySet() {
            var balance = new BalanceDto();
            balance.setDocuments(Set.of());

            assertThat(balance.getDocumentsIds()).isEmpty();
        }

        @Test
        @DisplayName("with documents, should return their ids")
        void givenDocuments_shouldReturnIds() {
            var other = new DocumentDto(UUID.fromString("55555555-5555-5555-5555-555555555555"), "b", "p", "t", 1L, "h");
            var balance = new BalanceDto();
            balance.setDocuments(Set.of(createDocumentDto(), other));

            assertThat(balance.getDocumentsIds()).containsExactlyInAnyOrder(DOCUMENT_ID, other.getId());
        }
    }
}
