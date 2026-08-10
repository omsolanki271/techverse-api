package com.om.blog.services.impl;

import com.om.blog.services.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        // File name
        String name = file.getOriginalFilename();

        if (name == null || !name.contains(".")) {
            throw new IOException("Invalid image file");
        }

        String extension = name.substring(name.lastIndexOf(".")).toLowerCase();

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".gif")
                && !extension.equals(".webp"))
        {
            throw new IOException("Invalid image format. Only JPG, JPEG, PNG, GIF and WEBP are allowed.");
        }

        String randomId = UUID.randomUUID().toString();

        String fileName  = randomId.concat(extension);

        // Full path
        String filepath = path + File.separator + fileName ;

        // Create folder if not created
        File file1 = new File(path);

        if (!file1.exists()) {
            file1.mkdirs();
        }

        // File copy
        Files.copy(file.getInputStream(), Paths.get(filepath));

        return fileName;
    }

    @Override
    public InputStream getResource(String path, String fileName) throws FileNotFoundException {

        String fullPath = path + File.separator + fileName;

        return  new FileInputStream(fullPath);
    }

    @Override
    public void deleteFile(String path, String fileName) throws IOException {
        String fullPath = path + File.separator + fileName;

        File file = new File(fullPath);

        if (file.exists()) {
            Files.delete(file.toPath());
        }
    }


}