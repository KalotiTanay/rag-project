package com.ragqa;

import com.ragqa.config.IngestionProperties;
import com.ragqa.exception.InvalidFileException;
import com.ragqa.service.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class FileValidatorTest {

    private FileValidator validator;

    @BeforeEach
    void setUp() {
        IngestionProperties props = new IngestionProperties();
        props.setMaxFileSizeMb(1);
        props.setAllowedFileTypes(List.of("pdf", "txt", "md"));
        validator = new FileValidator(props);
    }

    @Test
    void rejectsDisallowedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", new byte[10]);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void rejectsOversizedFile() {
        byte[] tooBig = new byte[2 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", tooBig);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("exceeds max size");
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void acceptsValidPdf() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[100]);

        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }
}
