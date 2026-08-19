package com.om.blog.controllers;

import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.MediaDto;
import com.om.blog.services.MediaService;
import com.om.blog.services.FileService;
import com.om.blog.repositories.MediaRepo;
import com.om.blog.entities.Media;
import com.om.blog.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MediaRepo mediaRepo;

    @Value("${project.image}")
    private String path;

    // upload images

    @PostMapping("/user/{userId}/upload")
    public ResponseEntity<MediaDto> uploadMedia(
            @RequestParam("image") MultipartFile image,
            @PathVariable Integer userId
    ) throws IOException
    {
        MediaDto mediaDto = mediaService.uploadMedia(image, userId);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    //get all
    @GetMapping("/")
    public ResponseEntity<List<MediaDto>> getAllMedia() {

        List<MediaDto> mediaList = mediaService.getAllMedia();

        return new ResponseEntity<>(mediaList, HttpStatus.OK);
    }


    //get by id

    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaDto> getMediaById(
            @PathVariable Integer mediaId
    )
    {

        MediaDto mediaDto = mediaService.getMediaById(mediaId);

        return new ResponseEntity<>(mediaDto, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MediaDto>> getMediaByUser(
            @PathVariable Integer userId
    )
    {
        List<MediaDto> mediaList = mediaService.getMediaByUser(userId);
        return new ResponseEntity<>(mediaList, HttpStatus.OK);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<ApiResponse> deleteMedia(
            @PathVariable Integer mediaId
    ) throws IOException
    {
        mediaService.deleteMedia(mediaId);
        return new ResponseEntity<>(new ApiResponse("Media deleted successfully", true), HttpStatus.OK);
    }

    @GetMapping("/file/{mediaId}")
    public void downloadMedia(
            @PathVariable("mediaId") Integer mediaId,
            HttpServletResponse response
    ) throws IOException {
        Media media = mediaRepo.findById(mediaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Media", "Media Id", mediaId));

        String userFolder = "user-" + media.getUser().getId();
        InputStream resource = fileService.getResource(path, userFolder, media.getFileName());

        String contentType = getContentType(media.getFileName());
        response.setContentType(contentType);
        StreamUtils.copy(resource, response.getOutputStream());
    }

    private String getContentType(String fileName) {
        String extension =
                fileName.substring(
                        fileName.lastIndexOf(".")
                ).toLowerCase();

        return switch (extension) {
            case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case ".png" -> MediaType.IMAGE_PNG_VALUE;
            case ".gif" -> MediaType.IMAGE_GIF_VALUE;
            case ".webp" -> "image/webp";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}