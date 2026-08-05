package com.TheQuestApp.TheQuestBack.controller;

import com.TheQuestApp.TheQuestBack.entity.User;
import com.TheQuestApp.TheQuestBack.entity.VideoContent;
import com.TheQuestApp.TheQuestBack.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "Imprtação de videos do Youtube")
@SecurityRequirement(name = "bearerAuth")
public class VideoController {

    private final VideoService videoService;

    public record ImportVideoRequest(
            @NotBlank(message = "URL do vídeo é obrigatória")
            String url
    ){}

    @GetMapping
    @Operation(summary = "Listar vídeos mportados pelo usuário")
    public ResponseEntity<List<VideoContent>> list(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(videoService.getUserVideos(user.getId()));
    }

    @PostMapping
    @Operation(summary = "Importar vídeo do youtube pelo link")
    public ResponseEntity<VideoContent> importVideo(@Valid @RequestBody ImportVideoRequest request,
                                                    @AuthenticationPrincipal User user){
        return ResponseEntity.ok(videoService.importVideo(request.url(), user));
    }
}
