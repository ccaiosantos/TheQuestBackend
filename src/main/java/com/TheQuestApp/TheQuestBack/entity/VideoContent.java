package com.TheQuestApp.TheQuestBack.entity;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "youtube_url", nullable = false)
    private String youtubeUrl;

    @Column(name = "video_id", nullable = false)
    private String videoId;            // ID extraído da URL (ex: dQw4w9WgXcQ)

    @Column
    private String title;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;         // Transcrição/legenda extraída

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;

    @PrePersist
    protected void onCreate() {
        importedAt = LocalDateTime.now();
    }
}
