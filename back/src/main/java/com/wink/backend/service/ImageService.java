package com.wink.backend.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

@Service
public class ImageService {

    private static final String UPLOAD_DIR = "uploads/chat-images/";

    /**
     * Base64 문자열을 이미지 파일로 저장하고
     * 저장된 파일명을 반환한다.
     */
    public String saveBase64Image(String base64String) throws IOException {

        if (base64String == null || base64String.isBlank()) return null;

        // "data:image/png;base64,..."
        if (base64String.contains(",")) {
            base64String = base64String.split(",")[1];
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        String fileName = System.currentTimeMillis() + "_" +
                UUID.randomUUID() + ".jpg";

        File outputFile = new File(UPLOAD_DIR + fileName);
        outputFile.getParentFile().mkdirs();

        Files.write(outputFile.toPath(), decodedBytes);

        return fileName;
    }
}
