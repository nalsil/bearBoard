package com.nalsil.bear.service;

import com.nalsil.bear.domain.youtube.YoutubeVideo;
import com.nalsil.bear.domain.youtube.YoutubeVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 유튜브 영상 서비스
 *
 * 유튜브 영상 조회 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeVideoService {

    private final YoutubeVideoRepository youtubeVideoRepository;

    /**
     * 기업별 공개 유튜브 영상 목록 조회 (정렬 순서대로)
     *
     * @param companyId 기업 ID
     * @return 유튜브 영상 목록
     */
    public Flux<YoutubeVideo> getVideosByCompanyId(Long companyId) {
        log.debug("기업 ID로 유튜브 영상 목록 조회: companyId={}", companyId);
        return youtubeVideoRepository.findByCompanyIdAndIsHiddenOrderByDisplayOrderAsc(companyId, false);
    }

    /**
     * 유튜브 영상 ID로 조회
     *
     * @param id 영상 ID
     * @return 유튜브 영상
     */
    public Mono<YoutubeVideo> getVideoById(Long id) {
        log.debug("유튜브 영상 ID로 조회: id={}", id);
        return youtubeVideoRepository.findById(id);
    }

    /**
     * 유튜브 URL에서 비디오 ID 추출
     *
     * @param url 유튜브 URL
     * @return 비디오 ID (추출 실패 시 null)
     */
    public String extractVideoId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // https://www.youtube.com/watch?v=VIDEO_ID
        if (url.contains("youtube.com/watch?v=")) {
            int index = url.indexOf("v=");
            if (index != -1) {
                String videoId = url.substring(index + 2);
                int ampersandIndex = videoId.indexOf('&');
                if (ampersandIndex != -1) {
                    videoId = videoId.substring(0, ampersandIndex);
                }
                return videoId;
            }
        }
        // https://youtu.be/VIDEO_ID
        else if (url.contains("youtu.be/")) {
            int index = url.indexOf("youtu.be/");
            if (index != -1) {
                String videoId = url.substring(index + 9);
                int questionIndex = videoId.indexOf('?');
                if (questionIndex != -1) {
                    videoId = videoId.substring(0, questionIndex);
                }
                return videoId;
            }
        }

        return null;
    }

    /**
     * 유튜브 썸네일 URL 생성
     *
     * @param videoId 비디오 ID
     * @return 썸네일 URL
     */
    public String getThumbnailUrl(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            return null;
        }
        return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
    }

    /**
     * 유튜브 URL 유효성 검증
     *
     * @param url 유튜브 URL
     * @return 유효한 URL이면 true
     */
    public boolean isValidYoutubeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        return url.contains("youtube.com/watch?v=") || url.contains("youtu.be/");
    }

    /**
     * 유튜브 영상 생성
     *
     * @param video 유튜브 영상 엔티티
     * @return 생성된 유튜브 영상
     */
    public Mono<YoutubeVideo> createVideo(YoutubeVideo video) {
        log.info("Creating YouTube video: title={}", video.getTitle());
        return youtubeVideoRepository.save(video);
    }

    /**
     * 유튜브 영상 수정
     *
     * @param video 유튜브 영상 엔티티
     * @return 수정된 유튜브 영상
     */
    public Mono<YoutubeVideo> updateVideo(YoutubeVideo video) {
        log.info("Updating YouTube video: id={}, title={}", video.getId(), video.getTitle());
        return youtubeVideoRepository.save(video);
    }

    /**
     * 유튜브 영상 삭제
     *
     * @param videoId 유튜브 영상 ID
     * @return 삭제 결과
     */
    public Mono<Void> deleteVideo(Long videoId) {
        log.info("Deleting YouTube video: id={}", videoId);
        return youtubeVideoRepository.deleteById(videoId);
    }
}
