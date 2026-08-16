package com.om.blog.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileService {

    String uploadImage(String path, String userFolder, MultipartFile file) throws IOException;
    InputStream getResource(String path, String userFolder, String fileName) throws FileNotFoundException;
    void deleteFile(String path, String userFolder, String fileName) throws IOException;
}
