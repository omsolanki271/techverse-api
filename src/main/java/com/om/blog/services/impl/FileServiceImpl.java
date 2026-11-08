
package com.om.blog.services.impl;

import com.om.blog.services.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, String userFolder,
                              MultipartFile file) throws IOException {

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

    // Build and validate file paths
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
