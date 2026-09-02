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
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    /** Own profile - shows the Edit Profile button. */
    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName());
        Profile profile = profileService.getOrCreate(user);
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("roleLabel", roleLabel(user));
        model.addAttribute("isOwnProfile", true);
        return "profile";
    }

    /**
     * Someone else's profile, read-only - reached from "View Profile" on the
     * applicants list or search results. Shows a Message button instead of
     * Edit, and the same resume/CV link if they've uploaded one.
     */
    @GetMapping("/{id}")
    public String viewOtherProfile(@PathVariable Long id, Authentication authentication, Model model) {
        User currentUser = userService.findByEmail(authentication.getName());

        if (id.equals(currentUser.getId())) {
            return "redirect:/profile"; // viewing your own ID - send to the real "own profile" route
        }

        User targetUser = userService.findById(id);
        Profile profile = profileService.getOrCreate(targetUser);
        model.addAttribute("user", targetUser);
        model.addAttribute("profile", profile);
        model.addAttribute("roleLabel", roleLabel(currentUser));
        model.addAttribute("isOwnProfile", false);
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

    @PostMapping(value = "/edit", consumes = "multipart/form-data")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile resume) {

        User user = userService.findByEmail(authentication.getName());
        userService.updateName(user, name);
        profileService.update(user, bio, phone, location, skills, image, resume);
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
