package com.ragqa.extraction;

import com.ragqa.exception.TextExtractionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfTextExtractor implements TextExtractor {

    private static final double MIN_TEXT_PAGE_RATIO = 0.5;

    @Override
    public boolean supports(String fileExtension) {
        return "pdf".equalsIgnoreCase(fileExtension);
    }

    @Override
    public List<PageText> extract(InputStream inputStream) throws IOException {
        List<PageText> pages = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            int pageCount = document.getNumberOfPages();
            if (pageCount == 0) {
                throw new TextExtractionException("PDF has no pages.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            int pagesWithText = 0;

            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(document);
                if (text != null && !text.isBlank()) {
                    pagesWithText++;
                }
                pages.add(new PageText(i, text == null ? "" : text.trim()));
            }

            double textRatio = (double) pagesWithText / pageCount;
            if (textRatio < MIN_TEXT_PAGE_RATIO) {
                throw new TextExtractionException(
                        "PDF appears to be scanned/image-based (only " + pagesWithText + "/" + pageCount
                                + " pages had extractable text). OCR is out of scope for MVP.");
            }
        }

        return pages;
    }
}
