package com.hirezen.service;

import com.hirezen.model.Profile;
import com.hirezen.model.User;
import com.hirezen.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private static final String UPLOAD_DIR = "uploads/profile-images";

    private final ProfileRepository profileRepository;

    /** Returns the user's profile, creating an empty one on first visit so every page has something to render. */
    @Transactional
    public Profile getOrCreate(User user) {
        return profileRepository.findByUser(user)
                .orElseGet(() -> profileRepository.save(Profile.builder().user(user).build()));
    }

    @Transactional
    public Profile update(User user, String bio, String phone, String location, String skills, MultipartFile image) {
        Profile profile = getOrCreate(user);
        profile.setBio(bio);
        profile.setPhone(phone);
        profile.setLocation(location);
        profile.setSkills(skills);

        if (image != null && !image.isEmpty()) {
            try {
                profile.setImageUrl(storeImage(user, image));
            } catch (IOException ex) {
                // Don't fail the whole save just because the photo didn't upload -
                // the text fields the user typed are still worth keeping.
                log.warn("Failed to store profile image for user {}: {}", user.getEmail(), ex.getMessage());
            }
        }

        return profileRepository.save(profile);
    }

    private String storeImage(User user, MultipartFile image) throws IOException {
        Path dir = Paths.get(UPLOAD_DIR);
        Files.createDirectories(dir);

        String filename = "user-" + user.getId() + "-" + System.currentTimeMillis() + extensionOf(image.getOriginalFilename());
        Path target = dir.resolve(filename);
        image.transferTo(target);

        return "/uploads/profile-images/" + filename;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) return "";
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
