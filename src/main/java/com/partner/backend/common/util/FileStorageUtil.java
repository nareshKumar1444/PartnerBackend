package com.partner.backend.common.util;

import com.partner.backend.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
public class FileStorageUtil {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".pdf"};

    public String store(MultipartFile file, String subFolder) {
        validateFile(file);

        try {
            Path uploadPath = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(storedFilename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return subFolder + "/" + storedFilename;
        } catch (IOException ex) {
            log.error("Failed to store file", ex);
            throw new BadRequestException("Failed to store file: " + ex.getMessage());
        }
    }

    public void delete(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.warn("Could not delete file: {}", filePath);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum of 10 MB");
        }
        String ext = getExtension(file.getOriginalFilename()).toLowerCase();
        boolean valid = false;
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (ext.equals(allowed)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BadRequestException("Unsupported file type. Allowed: jpg, jpeg, png, pdf");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".bin";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
