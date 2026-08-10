package com.om.blog.services;

import com.om.blog.payloads.MediaDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaService {

    MediaDto uploadMedia(MultipartFile file, Integer userId) throws IOException;

    List<MediaDto> getAllMedia();

    MediaDto getMediaById(Integer mediaId);

    List<MediaDto> getMediaByUser(Integer userId);

    void deleteMedia(Integer mediaId);
}
