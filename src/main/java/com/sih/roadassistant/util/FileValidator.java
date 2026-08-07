package com.sih.roadassistant.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public class FileValidator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Upload failed: File size exceeds the maximum limit of 5MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new RuntimeException("Upload failed: Only JPEG and PNG images are allowed.");
        }
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new RuntimeException("Upload failed: Corrupted image file.");
            }

            boolean isJpeg = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
            boolean isPng = header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47;

            if (!isJpeg && !isPng) {
                throw new RuntimeException("Upload blocked: Suspicious file signature detected.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Upload validation error: " + e.getMessage());
        }
    }
}