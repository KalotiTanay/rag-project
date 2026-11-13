package com.ragqa;

import com.ragqa.embedding.VectorFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VectorFormatTest {

    @Test
    void formatsFloatArrayAsPgvectorLiteral() {
        float[] vector = {0.1f, 0.2f, -0.3f};
        assertThat(VectorFormat.toLiteral(vector)).isEqualTo("[0.1,0.2,-0.3]");
    }

    @Test
    void emptyArray_producesEmptyBrackets() {
        assertThat(VectorFormat.toLiteral(new float[0])).isEqualTo("[]");
    }
}
