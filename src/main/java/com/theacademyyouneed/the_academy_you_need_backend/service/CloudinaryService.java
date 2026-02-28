package com.theacademyyouneed.the_academy_you_need_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads any file to Cloudinary.
     * Returns a map with: { "url": "...", "publicId": "..." }
     */
    public Map<String, String> upload(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",          "academy/" + folder,   // e.g. academy/videos
                            "resource_type",   "auto",                // auto-detects video/audio/image/pdf
                            "use_filename",    true,
                            "unique_filename", true
                    )
            );

            String url      = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            log.info("File uploaded to Cloudinary: {}", publicId);

            return Map.of(
                    "url",      url,
                    "publicId", publicId
            );

        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new RuntimeException("Échec de l'upload du fichier");
        }
    }

    /**
     * Deletes a file from Cloudinary by its public ID.
     * Call this when admin deletes content.
     */
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "auto")
            );
            log.info("File deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Cloudinary delete failed for {}: {}", publicId, e.getMessage());
            throw new RuntimeException("Échec de la suppression du fichier");
        }
    }
}