package com.onurbcd.cli.exception;

import com.onurbcd.cli.enums.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionTest {

    @Nested
    @DisplayName("constructor with error code and arguments")
    class ArgsConstructor {

        @Test
        @DisplayName("should format the message with the arguments, keep the error and have no cause")
        void givenErrorAndArgs_whenCreate_shouldFormatMessage() {
            var exception = new ApiException(ErrorCode.SEQUENCE_CHANGED, 1, 2);

            assertThat(exception.getError()).isEqualTo(ErrorCode.SEQUENCE_CHANGED);
            assertThat(exception.getMessage())
                    .isEqualTo("The sequence cannot be changed. Current value: '1'; new value: '2'");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("should keep the plain message when the error has no placeholders")
        void givenErrorWithoutPlaceholders_whenCreate_shouldKeepMessage() {
            var exception = new ApiException(ErrorCode.BILL_ALREADY_PAID);

            assertThat(exception.getError()).isEqualTo(ErrorCode.BILL_ALREADY_PAID);
            assertThat(exception.getMessage()).isEqualTo("Bill is already paid");
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("constructor with error code and cause")
    class CauseConstructor {

        @Test
        @DisplayName("should format the message with the cause message and keep the cause")
        void givenErrorAndCause_whenCreate_shouldUseCauseMessageAndKeepCause() {
            var cause = new IOException("disk full");

            var exception = new ApiException(ErrorCode.STORAGE_FILE_SAVE, cause);

            assertThat(exception.getError()).isEqualTo(ErrorCode.STORAGE_FILE_SAVE);
            assertThat(exception.getMessage()).isEqualTo("Storage file save error: disk full");
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("notFound")
    class NotFound {

        @Test
        @DisplayName("should build an ENTITY_DOES_NOT_EXIST exception with the formatted id")
        void givenId_whenNotFound_shouldBuildEntityDoesNotExist() {
            var exception = ApiException.notFound(ID);

            assertThat(exception.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST);
            assertThat(exception.getMessage())
                    .isEqualTo("Entity with id '" + ID + "' does not exist in the database");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("should return a new instance on every call")
        void givenSameId_whenNotFoundTwice_shouldReturnDistinctInstances() {
            var id = UUID.randomUUID();

            assertThat(ApiException.notFound(id)).isNotSameAs(ApiException.notFound(id));
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should return the exception itself so it works as a Supplier")
        void givenException_whenGet_shouldReturnSameInstance() {
            var exception = new ApiException(ErrorCode.NO_ROWS_DELETED);

            assertThat(exception.get()).isSameAs(exception);
        }
    }

    @Test
    @DisplayName("should be an unchecked exception")
    void givenException_shouldBeRuntimeException() {
        assertThat(new ApiException(ErrorCode.NO_ROWS_DELETED)).isInstanceOf(RuntimeException.class);
    }
}
