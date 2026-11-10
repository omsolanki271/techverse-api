package com.om.blog.services;

import com.om.blog.payloads.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileService {

    // Cloudinary Operations
    CloudinaryResponse uploadImageToCloudinary(MultipartFile file, String folder) throws IOException;

    void deleteImageFromCloudinary(String publicId) throws IOException;

    // Legacy Local Storage Operations (preserved for backward compatibility)
    String uploadImage(String path, String userFolder, MultipartFile file) throws IOException;

    InputStream getResource(String path, String userFolder, String fileName) throws FileNotFoundException;

    void deleteFile(String path, String userFolder, String fileName) throws IOException;
}
