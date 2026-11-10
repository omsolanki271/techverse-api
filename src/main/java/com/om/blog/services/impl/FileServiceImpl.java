package com.om.blog.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.om.blog.payloads.CloudinaryResponse;
import com.om.blog.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private Cloudinary cloudinary;

    // ==========================================
    // CLOUDINARY FILE OPERATIONS
    // ==========================================

    @Override
    public CloudinaryResponse uploadImageToCloudinary(MultipartFile file, String folder) throws IOException {
        validateImageFile(file);

        if (cloudinary == null || cloudinary.config.cloudName == null || cloudinary.config.cloudName.isBlank()) {
            throw new IOException("Cloudinary credentials are not configured. Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET.");
        }

        Map<String, Object> options = new HashMap<>();
        if (folder != null && !folder.isBlank()) {
            options.put("folder", folder);
        }
        options.put("resource_type", "image");

        @SuppressWarnings("rawtypes")
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

        String publicId = (String) uploadResult.get("public_id");
        String secureUrl = (String) uploadResult.get("secure_url");
        String format = (String) uploadResult.get("format");
        long bytes = uploadResult.get("bytes") instanceof Number num ? num.longValue() : file.getSize();

        return new CloudinaryResponse(publicId, secureUrl, format, bytes);
    }

    @Override
    public void deleteImageFromCloudinary(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        if (cloudinary == null || cloudinary.config.cloudName == null || cloudinary.config.cloudName.isBlank()) {
            System.err.println("Warning: Skipping Cloudinary deletion because credentials are not configured.");
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Warning: Cloudinary image deletion failed for publicId [" + publicId + "]: " + e.getMessage());
        }
    }

    // ==========================================
    // LEGACY LOCAL STORAGE OPERATIONS
    // ==========================================

    @Override
    public String uploadImage(String path, String userFolder,
                              MultipartFile file) throws IOException {

        validateImageFile(file);

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(
                originalName.lastIndexOf(".")
        ).toLowerCase(Locale.ROOT);

        String fileName = UUID.randomUUID() + extension;

        Path folderPath = resolveSafePath(path, userFolder, "");

        // Create user folder if it does not exist
        Files.createDirectories(folderPath);

        Path filePath = resolveSafePath(path, userFolder, fileName);

        // Save uploaded image
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath);
        }

        return fileName;
    }

    @Override
    public InputStream getResource(String path, String userFolder,
                                   String fileName)
            throws FileNotFoundException {

        Path filePath = resolveSafePath(path, userFolder, fileName);

        return new FileInputStream(filePath.toFile());
    }

    @Override
    public void deleteFile(String path, String userFolder,
                           String fileName) throws IOException {

        Path filePath = resolveSafePath(path, userFolder, fileName);

        // Delete only if the file exists
        Files.deleteIfExists(filePath);
    }

    // Common file validation helper
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Please select an image.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.contains(".")) {
            throw new IOException("Invalid image file.");
        }

        String extension = originalName.substring(
                originalName.lastIndexOf(".")
        ).toLowerCase(Locale.ROOT);

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".gif")
                && !extension.equals(".webp")) {

            throw new IOException(
                    "Only JPG, JPEG, PNG, GIF and WEBP images are allowed."
            );
        }
    }

    // Build and validate safe file paths for local storage
    private Path resolveSafePath(String path, String userFolder,
                                 String fileName) {

        Path basePath = Paths.get(path).toAbsolutePath().normalize();

        Path folderPath = basePath;

        if (userFolder != null && !userFolder.isBlank()) {
            folderPath = basePath.resolve(userFolder).normalize();
        }

        if (!folderPath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid folder path.");
        }

        if (fileName == null || fileName.isBlank()) {
            return folderPath;
        }

        Path filePath = folderPath.resolve(fileName).normalize();

        if (!filePath.startsWith(folderPath)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        return filePath;
    }
}
