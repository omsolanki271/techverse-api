package com.om.blog.services.impl;

import com.om.blog.entities.Media;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceAlreadyInUseException;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.MediaDto;
import com.om.blog.repositories.MediaRepo;
import com.om.blog.repositories.PostRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.FileService;
import com.om.blog.services.MediaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private PostRepo postRepo;

    @Override
    public MediaDto uploadMedia(MultipartFile file, Integer userId) throws IOException {

        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId ));

        checkUserOwnerOrAdmin(user);

        String userFolder = "user-" + userId;

        String fileName = fileService.uploadImage(path,userFolder,file);

        Media media = new Media();

        media.setFileName(fileName);
        media.setFileType(file.getContentType());
        media.setFilePath(path + userFolder);
        media.setUploadedDate(LocalDateTime.now());
        media.setUser(user);

        Media savedMedia = mediaRepo.save(media);

        return modelMapper.map(savedMedia, MediaDto.class);
    }


    @Override
    public List<MediaDto> getAllMedia() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = isAdmin(authentication);

        List<Media> mediaList;

        if (isAdmin) {
            mediaList = mediaRepo.findAll();
        }
        else {
            User user = getLoggedInUser();
            mediaList = mediaRepo.findByUser(user);
        }

        return mediaList.stream()
                .map(media -> modelMapper.map(media, MediaDto.class))
                .toList();
    }


    @Override
    public MediaDto getMediaById(Integer mediaId) {

        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("Media", "Media Id", mediaId));

        checkMediaOwnerOrAdmin(media);

        return modelMapper.map(media, MediaDto.class);
    }


    @Override
    public List<MediaDto> getMediaByUser(Integer userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId));

        checkUserOwnerOrAdmin(user);

        List<Media> mediaList = mediaRepo.findByUser(user);

        return mediaList.stream()
                .map(media -> modelMapper.map(media, MediaDto.class))
                .toList();
    }


    @Override
    public void deleteMedia(Integer mediaId) throws IOException {

        Media media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", "Media Id", mediaId));

        checkMediaOwnerOrAdmin(media);

        List<Post> posts = postRepo.findByMedia(media);

        if (!posts.isEmpty()) {
            throw new ResourceAlreadyInUseException
                    ("Media cannot be deleted because it is currently used by one or more posts.");
        }

        String userFolder = "user-" + media.getUser().getId();

        fileService.deleteFile(path, userFolder, media.getFileName());

        mediaRepo.delete(media);
    }


    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "Email", 0));
    }


    private boolean isAdmin(Authentication authentication) {

        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));
    }


    private void checkUserOwnerOrAdmin(User user) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(authentication)) {
            return;
        }

        String loggedInEmail = authentication.getName();

        if (!user.getEmail().equals(loggedInEmail)) {
            throw new AccessDeniedException(
                    "You are not allowed to access this user's media"
            );
        }
    }


    private void checkMediaOwnerOrAdmin(Media media) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (isAdmin(authentication)) {
            return;
        }

        String loggedInEmail = authentication.getName();

        if (!media.getUser().getEmail().equals(loggedInEmail)) {
            throw new AccessDeniedException(
                    "You are not allowed to access this media"
            );
        }
    }
}