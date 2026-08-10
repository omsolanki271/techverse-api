package com.om.blog.controllers;

import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.MediaDto;
import com.om.blog.services.MediaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

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
}