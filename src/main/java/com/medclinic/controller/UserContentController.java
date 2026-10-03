package com.medclinic.controller;

import com.medclinic.service.AvatarStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class UserContentController {
    private final AvatarStorageService avatarStorageService;

    @GetMapping("/user-content/avatars/{filename:.+}")
    public ResponseEntity<?> avatar(@PathVariable String filename) {
        var avatar = avatarStorageService.loadAvatar(filename);
        return ResponseEntity.ok()
                .contentType(avatar.mediaType())
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .body(avatar.resource());
    }
}
