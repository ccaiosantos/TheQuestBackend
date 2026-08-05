package com.TheQuestApp.TheQuestBack.service;

import com.TheQuestApp.TheQuestBack.entity.User;
import com.TheQuestApp.TheQuestBack.entity.VideoContent;
import com.TheQuestApp.TheQuestBack.repository.VideoContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final VideoContentRepository videoContentRepository;
    //o youtube tem diversos tipos de url então esse Pattern que é do regex ele captura o id do video sem se preocupar com estrutura
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
    );

    @Transactional
    public VideoContent importVideo(String youtubeUrl, User user){
        String videoId = extractVideoId(youtubeUrl);

        if (videoId == null){
            throw  new IllegalArgumentException("URL do YouTube inválida. Use o formato: https://www.youtube.com/watch?v=XXXXXXXXXXX");
        }

        if (videoContentRepository.existsByYoutubeUrlAndUserId(youtubeUrl, user.getId())){
            throw new IllegalArgumentException("Você já importou este vídeo");
        }

        var video = VideoContent.builder()
                .user(user)
                .youtubeUrl(youtubeUrl)
                .videoId(videoId)
                .title("Video"+ videoId)
                .thumbnailUrl("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg")
                .transcript("")
                .build();

        VideoContent saved = videoContentRepository.save(video);
        log.info("Vídeo importado: {} para usuário: {}", videoId, user.getId());
        return saved;

    }

    public List<VideoContent> getUserVideos(Long userId){
        return videoContentRepository.findByUserIdOrderByImportedAtDesc(userId);
    }

    public VideoContent getVideoById(Long id, Long userId){
        return videoContentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado."));
    }

    private String extractVideoId(String url){
        if (url == null) return null;
        Matcher matcher = YOUTUBE_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null; // retorna o que foi capturado pelo primeiro grupo do Regex, que é justamente o ID de 11 caracteres entre parênteses no padrão.
    }


}
