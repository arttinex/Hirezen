package com.hirezen.controller;

import com.hirezen.model.Profile;
import com.hirezen.model.User;
import com.hirezen.service.ProfileService;
import com.hirezen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName());
        Profile profile = profileService.getOrCreate(user);
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("roleLabel", roleLabel(user));
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName());
        Profile profile = profileService.getOrCreate(user);
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("roleLabel", roleLabel(user));
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skills) {

        User user = userService.findByEmail(authentication.getName());
        profileService.update(user, bio, phone, location, skills);
        return "redirect:/profile";
    }

    private String roleLabel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Admin";
            case RECRUITER -> "Recruiter";
            case JOB_SEEKER -> "Job Seeker";
        };
    }
}
