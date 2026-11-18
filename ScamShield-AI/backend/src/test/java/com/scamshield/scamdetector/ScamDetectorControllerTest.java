package com.scamshield.scamdetector;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.scamshield.scamdetector.exception.ScamDetectorException;
import com.scamshield.scamdetector.model.AiAnalysisResult;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.RiskStatus;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.scamdetector.service.GeminiAnalysisService;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

@SpringBootTest(
        properties = {
            "spring.data.mongodb.uri=mongodb://localhost:27017/scamshield_test",
            "app.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
            "app.auth.cookie-secure=false",
            "app.cors.allowed-origin=http://localhost:5173"
        })
@AutoConfigureMockMvc
class ScamDetectorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScamAnalysisRepository repository;

    @MockitoBean
    private GeminiAnalysisService geminiAnalysisService;

    @Test
    void reportLookupUsesAuthenticatedUserInsteadOfQueryParameter() throws Exception {
        when(repository.findByIdAndUserId("report-id", "authenticated-user")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/scam-detector/report-id?userId=another-user")
                        .with(user(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Analysis report was not found."));

        verify(repository).findByIdAndUserId("report-id", "authenticated-user");
    }

    @Test
    void geminiUnavailableDoesNotReturnASuccessfulAnalysis() throws Exception {
        when(geminiAnalysisService.analyze(
                        any(InputType.class),
                        anyString(),
                        nullable(String.class),
                        nullable(byte[].class),
                        anyList()))
                .thenThrow(new ScamDetectorException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "AI analysis is not configured. Set GEMINI_API_KEY on the backend."));

        mockMvc.perform(post("/api/scam-detector/analyze/text")
                        .with(user(authenticatedUser()))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"text\":\"Please review this job offer.\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(
                        "AI analysis is not configured. Set GEMINI_API_KEY on the backend."));
    }

    @Test
    void textAnalysisReturnsOnlyThePersistedAiReport() throws Exception {
        stubSuccessfulAnalysis();

        mockMvc.perform(post("/api/scam-detector/analyze/text")
                        .with(user(authenticatedUser()))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"text\":\"Please review this job offer.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("saved-report"))
                .andExpect(jsonPath("$.inputType").value("TEXT"))
                .andExpect(jsonPath("$.riskStatus").value("NEEDS_REVIEW"))
                .andExpect(jsonPath("$.companyVerificationStatus").value("UNVERIFIED"));
    }

    @Test
    void emailAnalysisUsesValidatedEmailFields() throws Exception {
        stubSuccessfulAnalysis();

        mockMvc.perform(post("/api/scam-detector/analyze/email")
                        .with(user(authenticatedUser()))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "subject": "Job opportunity",
                                  "senderEmail": "recruiter@example.com",
                                  "body": "Please review this email."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputType").value("EMAIL"))
                .andExpect(jsonPath("$.inputSummary").value("Email review"));
    }

    @Test
    void urlAnalysisRejectsPrivateNetworksAndDoesNotFetchPublicUrls() throws Exception {
        mockMvc.perform(post("/api/scam-detector/analyze/url")
                        .with(user(authenticatedUser()))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"url\":\"http://127.0.0.1/apply\"}"))
                .andExpect(status().isBadRequest());

        stubSuccessfulAnalysis();
        mockMvc.perform(post("/api/scam-detector/analyze/url")
                        .with(user(authenticatedUser()))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"url\":\"https://8.8.8.8/apply\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputType").value("URL"))
                .andExpect(jsonPath("$.companyVerificationStatus").value("UNVERIFIED"));
    }

    @Test
    void imageUploadIsContentValidatedAndAnalyzedByGemini() throws Exception {
        stubSuccessfulAnalysis();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", output);
        MockMultipartFile file =
                new MockMultipartFile("file", "offer.png", "text/plain", output.toByteArray());

        mockMvc.perform(multipart("/api/scam-detector/analyze/file")
                        .file(file)
                        .with(user(authenticatedUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputType").value("IMAGE"));
    }

    @Test
    void pdfUploadExtractsTextAndReturnsRealAnalysis() throws Exception {
        stubSuccessfulAnalysis();
        MockMultipartFile file =
                new MockMultipartFile("file", "offer.pdf", "application/pdf", createPdf());

        mockMvc.perform(multipart("/api/scam-detector/analyze/file")
                        .file(file)
                        .with(user(authenticatedUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputType").value("PDF"));
    }

    @Test
    void docxUploadExtractsTextAndReturnsRealAnalysis() throws Exception {
        stubSuccessfulAnalysis();
        MockMultipartFile file =
                new MockMultipartFile("file", "offer.docx", "application/octet-stream", createDocx());

        mockMvc.perform(multipart("/api/scam-detector/analyze/file")
                        .file(file)
                        .with(user(authenticatedUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputType").value("DOCX"));
    }

    @Test
    void rejectsEmptyTextAndAFileWithMismatchedContent() throws Exception {
        mockMvc.perform(post("/api/scam-detector/analyze/text")
                        .with(user(authenticatedUser()))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"text\":\"   \"}"))
                .andExpect(status().isBadRequest());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "offer.pdf",
                "application/pdf",
                new byte[] {0x50, 0x4b, 0x03, 0x04});
        mockMvc.perform(multipart("/api/scam-detector/analyze/file")
                        .file(file)
                        .with(user(authenticatedUser()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    private void stubSuccessfulAnalysis() {
        when(geminiAnalysisService.analyze(
                        any(InputType.class),
                        anyString(),
                        nullable(String.class),
                        nullable(byte[].class),
                        anyList()))
                .thenReturn(new AiAnalysisResult(
                        RiskStatus.NEEDS_REVIEW,
                        40,
                        "Some details need independent review.",
                        List.of(),
                        List.of(),
                        "UNVERIFIED",
                        List.of("Verify details independently."),
                        List.of()));
        doAnswer(invocation -> {
            ScamAnalysis analysis = invocation.getArgument(0);
            analysis.setId("saved-report");
            return analysis;
        }).when(repository).save(any(ScamAnalysis.class));
    }

    private UserPrincipal authenticatedUser() {
        User user = new User(
                "Morgan Lee",
                "morgan@example.com",
                "password-hash",
                UserRole.USER,
                Instant.parse("2026-01-01T00:00:00Z"));
        user.setId("authenticated-user");
        return new UserPrincipal(user);
    }

    private byte[] createPdf() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Job application processing fee");
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] createDocx() throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Job application processing fee");
            document.write(output);
            return output.toByteArray();
        }
    }
}
