package com.TheQuestApp.TheQuestBack.repository;

import com.TheQuestApp.TheQuestBack.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Lista todos os documentos de um usuário, mais recentes primeiro
    List<Document> findByUserIdOrderByUploadedAtDesc(Long userId);

    // Garante que o documento pertence ao usuário (evita acesso cruzado)
    Optional<Document> findByIdAndUserId(Long id, Long userId);

}
