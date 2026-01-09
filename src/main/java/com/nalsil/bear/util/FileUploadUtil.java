package com.nalsil.bear.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * FileUploadUtil
 * 파일 업로드 검증 및 저장 유틸리티
 */
@Slf4j
@Component
public class FileUploadUtil {

    /**
     * 파일 업로드 기본 디렉토리
     */
    @Value("${app.file.upload.base-dir}")
    private String baseDir;

    /**
     * 최대 파일 크기 (20MB)
     */
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    /**
     * 허용되는 파일 MIME 타입
     */
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/zip"
    );

    /**
     * 허용되는 파일 확장자
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".jpg", ".jpeg", ".png", ".gif", ".zip"
    );

    /**
     * 파일 검증 및 저장
     *
     * @param filePart 업로드 파일
     * @param subDirectory 하위 디렉토리 (예: "posts", "products")
     * @return 저장된 파일 경로 (Mono<String>)
     */
    public Mono<String> saveFile(FilePart filePart, String subDirectory) {
        return Mono.fromCallable(() -> {
            // 파일 검증
            validateFile(filePart);

            // 저장 디렉토리 생성
            Path uploadPath = createUploadDirectory(subDirectory);

            // 고유한 파일명 생성
            String uniqueFileName = generateUniqueFileName(filePart.filename());

            // 파일 저장
            Path filePath = uploadPath.resolve(uniqueFileName);
            filePart.transferTo(filePath.toFile()).block();

            log.info("File saved: {}", filePath);

            // 상대 경로 반환 (데이터베이스 저장용)
            return String.format("/uploads/%s/%s", subDirectory, uniqueFileName);
        });
    }

    /**
     * 파일 검증
     *
     * @param filePart 업로드 파일
     * @throws IllegalArgumentException 검증 실패 시
     */
    private void validateFile(FilePart filePart) {
        String fileName = filePart.filename();
        String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : "";

        // 파일 확장자 검증
        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    String.format("허용되지 않는 파일 확장자입니다: %s. 허용 확장자: %s",
                            extension, ALLOWED_EXTENSIONS)
            );
        }

        // MIME 타입 검증
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    String.format("허용되지 않는 파일 타입입니다: %s", contentType)
            );
        }

        log.info("File validation passed: {} ({})", fileName, contentType);
    }

    /**
     * 업로드 디렉토리 생성
     *
     * @param subDirectory 하위 디렉토리
     * @return 업로드 디렉토리 경로
     * @throws IOException 디렉토리 생성 실패 시
     */
    private Path createUploadDirectory(String subDirectory) throws IOException {
        Path uploadPath = Paths.get(baseDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Upload directory created: {}", uploadPath);
        }
        return uploadPath;
    }

    /**
     * 고유한 파일명 생성
     * 패턴: yyyyMMdd_HHmmss_UUID_원본파일명
     *
     * @param originalFileName 원본 파일명
     * @return 고유한 파일명
     */
    private String generateUniqueFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        String nameWithoutExtension = originalFileName.substring(0, originalFileName.lastIndexOf('.'));

        return String.format("%s_%s_%s%s", timestamp, uuid, sanitizeFileName(nameWithoutExtension), extension);
    }

    /**
     * 파일 확장자 추출
     *
     * @param fileName 파일명
     * @return 확장자 (점 포함)
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    /**
     * 파일명 정제 (특수문자 제거)
     *
     * @param fileName 파일명
     * @return 정제된 파일명
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
    }

    /**
     * 파일 삭제
     *
     * @param filePath 파일 경로
     * @return 삭제 성공 여부 (Mono<Boolean>)
     */
    public Mono<Boolean> deleteFile(String filePath) {
        return Mono.fromCallable(() -> {
            try {
                Path path = Paths.get(baseDir, filePath.replace("/uploads/", ""));
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("File deleted: {}", path);
                    return true;
                }
                log.warn("File not found for deletion: {}", path);
                return false;
            } catch (IOException e) {
                log.error("Failed to delete file: {}", filePath, e);
                return false;
            }
        });
    }
}
