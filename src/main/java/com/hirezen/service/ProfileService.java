package com.hirezen.service;

import com.hirezen.model.Profile;
import com.hirezen.model.User;
import com.hirezen.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    /** Returns the user's profile, creating an empty one on first visit so every page has something to render. */
    @Transactional
    public Profile getOrCreate(User user) {
        return profileRepository.findByUser(user)
                .orElseGet(() -> profileRepository.save(Profile.builder().user(user).build()));
    }

    @Transactional
    public Profile update(User user, String bio, String phone, String location, String skills) {
        Profile profile = getOrCreate(user);
        profile.setBio(bio);
        profile.setPhone(phone);
        profile.setLocation(location);
        profile.setSkills(skills);
        return profileRepository.save(profile);
    }
}
