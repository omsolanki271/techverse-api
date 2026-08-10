package com.om.blog.services.impl;

import com.om.blog.entities.Media;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.MediaDto;
import com.om.blog.repositories.MediaRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.FileService;
import com.om.blog.services.MediaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaRepo mediaRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${project.image}")
    private String path;

    @Override
    public MediaDto uploadMedia(MultipartFile file, Integer userId) throws IOException {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId ));

        String fileName = fileService.uploadImage(path, file);

        Media media = new Media();

        media.setFileName(fileName);
        media.setFileType(file.getContentType());
        media.setFilePath(path);
        media.setUploadedDate(LocalDateTime.now());
        media.setUser(user);

        Media savedMedia = mediaRepo.save(media);

        return modelMapper.map(savedMedia, MediaDto.class);
    }


    @Override
    public List<MediaDto> getAllMedia() {
        List<Media> mediaList = mediaRepo.findAll();
        return mediaList.stream().map(media -> modelMapper.map(media, MediaDto.class)).toList();
    }


    @Override
    public MediaDto getMediaById(Integer mediaId) {

        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("Media", "Media Id", mediaId));
        return modelMapper.map(media, MediaDto.class);
    }


    @Override
    public List<MediaDto> getMediaByUser(Integer userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId));

        List<Media> mediaList = mediaRepo.findByUser(user);

        return mediaList.stream()
                .map(media -> modelMapper.map(media, MediaDto.class))
                .toList();
    }


    @Override
    public void deleteMedia(Integer mediaId) {
        Media media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", "Media Id", mediaId));
        mediaRepo.delete(media);
    }
}
