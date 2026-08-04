package com.TheQuestApp.TheQuestBack.repository;

import com.TheQuestApp.TheQuestBack.entity.VideoContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoContentRepository extends JpaRepository<VideoContent, Long> {
    List<VideoContent> findByUserIdOrderByImportedAtDesc(Long userId);
    Optional<VideoContent> findByIdAndUserId(Long id, Long userId);
    boolean existsByYoutubeUrlAndUserId(String youtubeUrl, Long userId);

}
