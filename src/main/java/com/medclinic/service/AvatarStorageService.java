package com.medclinic.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {
    String storeAvatar(MultipartFile file, String currentAvatarUrl);
    void deleteAvatar(String avatarUrl);
    AvatarResource loadAvatar(String filename);

    record AvatarResource(Resource resource, MediaType mediaType) {}
}
