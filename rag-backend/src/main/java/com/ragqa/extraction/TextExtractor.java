package com.ragqa.extraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface TextExtractor {

    boolean supports(String fileExtension);

    List<PageText> extract(InputStream inputStream) throws IOException;
}
