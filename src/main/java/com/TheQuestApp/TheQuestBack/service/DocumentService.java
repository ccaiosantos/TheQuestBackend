package com.TheQuestApp.TheQuestBack.service;

import com.TheQuestApp.TheQuestBack.entity.Document;
import com.TheQuestApp.TheQuestBack.entity.User;
import com.TheQuestApp.TheQuestBack.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Value("${upload.directory}")
    private String uploadDirectory;

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; //50MB

    @Transactional
    public Document uploadDocument(MultipartFile file, User user) throws IOException {

        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }



        String uniqueFileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(uniqueFileName);
        file.transferTo(filePath.toFile());

        log.info("PDF salvo em: {}", filePath);

        String extractedText;
        int pageCount;

        try (PDDocument pdf = Loader.loadPDF(filePath.toFile())){
            pageCount = pdf.getNumberOfPages();
            var stripper = new PDFTextStripper();
            extractedText = stripper.getText(pdf);
            log.warn("O PDFBox extrai {} caracterers de {} páginas", extractedText.length(), pageCount);

        }catch (IOException e){
            log.info("Não foi possível extrair texto do PDF: {}", e.getMessage());
            extractedText = "";
            pageCount= 0;
        }

        var document = Document.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .fileSizeBytes(file.getSize())
                .pageCount(pageCount)
                .extractedText(extractedText)
                .build();

        return documentRepository.save(document);
    }
    public List<Document> getUserDocuments(Long userId){
        return documentRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }
    public Document getDocumentById(Long id, Long userId){
        return documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado"));
    }

    private void validateFile(MultipartFile file){
        if (file == null || file.isEmpty()){
            throw new IllegalArgumentException("O arquivo não pode ser vazio");
        }
        if (file.getSize() > MAX_FILE_SIZE){
            throw new IllegalArgumentException("O arquivo não pode ser maior do que 50MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Apenas arquivos PDF são aceitos.");
        }
    }

    private String sanitizeFileName(String originalName) {
        if (originalName == null) return "documento.pdf";
        // Remove caracteres especiais para evitar problemas no filesystem
        return originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
