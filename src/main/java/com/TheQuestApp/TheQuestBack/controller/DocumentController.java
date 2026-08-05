package com.TheQuestApp.TheQuestBack.controller;

import com.TheQuestApp.TheQuestBack.entity.Document;
import com.TheQuestApp.TheQuestBack.entity.User;
import com.TheQuestApp.TheQuestBack.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documentos", description = "Upload e listagem de PDFs")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar PDF")
    public ResponseEntity<Document> upload(@RequestParam("file")MultipartFile file,
                                           @AuthenticationPrincipal User user)throws IOException{
        return ResponseEntity.ok(documentService.uploadDocument(file, user));
    }

    @GetMapping
    @Operation(summary = "Listar PDFs do Usuário")
    public ResponseEntity<List<Document>> list(@AuthenticationPrincipal User user ){
        return ResponseEntity.ok(documentService.getUserDocuments(user.getId()));
    }
    @GetMapping
    @Operation(summary = "Detalhes de um PDF")
    public ResponseEntity<Document> getById(@PathVariable Long id, @AuthenticationPrincipal User user){
        return ResponseEntity.ok(documentService.getDocumentById(id, user.getId()));
    }
}
