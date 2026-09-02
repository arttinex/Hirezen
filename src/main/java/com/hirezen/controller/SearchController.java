package com.hirezen.controller;

import com.hirezen.model.User;
import com.hirezen.service.JobService;
import com.hirezen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final UserService userService;
    private final JobService jobService;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Authentication authentication, Model model) {
        User currentUser = userService.findByEmail(authentication.getName());
        model.addAttribute("roleLabel", roleLabel(currentUser));
        model.addAttribute("query", q);

        if (q != null && !q.isBlank()) {
            String trimmed = q.trim();
            model.addAttribute("matchedUsers", userService.search(trimmed));
            model.addAttribute("matchedJobs", jobService.search(trimmed));
        }

        return "search-results";
    }

    private String roleLabel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Admin";
            case RECRUITER -> "Recruiter";
            case JOB_SEEKER -> "Job Seeker";
        };
    }
}
