package com.curriculo.inteligente.export;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfExportService {

    public record PdfFile(byte[] bytes, int pages) {
    }

    public PdfFile render(ResumeDocument document, byte[] photo) {
        String photoUri = photo == null || photo.length == 0
                ? null
                : "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(photo);
        String html = ResumeHtmlRenderer.render(document, photoUri);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerArial(builder, "C:/Windows/Fonts/arial.ttf", 400, FontStyle.NORMAL);
            registerArial(builder, "C:/Windows/Fonts/arialbd.ttf", 700, FontStyle.NORMAL);
            registerArial(builder, "C:/Windows/Fonts/ariali.ttf", 400, FontStyle.ITALIC);
            registerArial(builder, "C:/Windows/Fonts/arialbi.ttf", 700, FontStyle.ITALIC);
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            byte[] bytes = strip(out.toByteArray());
            return new PdfFile(bytes, pages(bytes));
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao gerar o PDF.");
        }
    }

    private static void registerArial(PdfRendererBuilder builder, String path, int weight, FontStyle style) {
        Path file = Path.of(path);
        if (Files.exists(file)) {
            builder.useFont(file.toFile(), "Arial", weight, style, true);
        }
    }

    public int pages(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            return document.getNumberOfPages();
        }
    }

    private byte[] strip(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.getDocumentInformation().setTitle(null);
            document.getDocumentInformation().setAuthor(null);
            document.getDocumentInformation().setSubject(null);
            document.getDocumentInformation().setKeywords(null);
            document.getDocumentInformation().setCreator(null);
            document.save(out);
            return out.toByteArray();
        }
    }
}
