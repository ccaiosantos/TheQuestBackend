package com.TheQuestApp.TheQuestBack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;           // Caminho no servidor

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;      // Texto extraído pelo PDFBox

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // Helper: tamanho em MB para a resposta da API
    @Transient
    public double getFileSizeMb() {
        if (fileSizeBytes == null) return 0;
        return Math.round(fileSizeBytes / (1024.0 * 1024.0) * 10.0) / 10.0;
    }

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }



}
