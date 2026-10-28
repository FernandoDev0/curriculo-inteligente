package com.curriculo.inteligente.export;

import com.curriculo.inteligente.common.ApiException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfExportServiceTest {

    private final PdfExportService pdf = new PdfExportService();

    @Test
    void shortResumeFitsOnOnePage() throws Exception {
        PdfExportService.PdfFile file = pdf.render(sample(2), null);
        assertTrue(new String(file.bytes(), StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        assertEquals(1, file.pages());
        assertEquals(1, pdf.pages(file.bytes()));
    }

    @Test
    void overflowingResumeIsReportedAsMoreThanOnePage() {
        PdfExportService.PdfFile file = pdf.render(sample(80), null);
        assertTrue(file.pages() > 1);
        ApiException error = assertThrows(ApiException.class, () -> refuse(file.pages()));
        assertEquals("PAGE_OVERFLOW", error.code());
        assertEquals(file.pages(), error.pageCount());
    }

    private static void refuse(int pages) {
        if (pages != 1) {
            throw new ApiException(409, "PAGE_OVERFLOW", "O currículo não cabe em uma página.", pages,
                    List.of("Encurte o resumo."));
        }
    }

    private static ResumeDocument sample(int bullets) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < bullets; i++) {
            lines.add("Preparava pães, organizava a produção e conferia o estoque do dia na padaria fictícia, item " + i + ".");
        }
        ResumeDocument.ExperienceLine job = new ResumeDocument.ExperienceLine(
                "1", "Padaria Sol", "Padeira", 2022, 3, null, null, true, null,
                String.join("\n", lines), "Forno", null, "DETAILED");
        ResumeDocument.Header header = new ResumeDocument.Header(
                "Ana Souza", "Padeira", "Campinas", "SP", "ONSITE",
                "ana@example.com", null, null, null, null, null, null, false, false);
        return new ResumeDocument(header, "Padeira com experiência em produção.", List.of(job),
                List.of(new ResumeDocument.SkillLine("Forno", "TOOL")), List.of(), List.of(), List.of(), List.of());
    }
}
