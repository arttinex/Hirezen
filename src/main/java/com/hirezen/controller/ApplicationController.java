package com.hirezen.controller;

import com.hirezen.model.Job;
import com.hirezen.model.User;
import com.hirezen.service.ApplicationService;
import com.hirezen.service.JobService;
import com.hirezen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobService jobService;
    private final UserService userService;

    /** Restricted to JOB_SEEKER in SecurityConfiguration. */
    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id, Authentication authentication) {
        User seeker = userService.findByEmail(authentication.getName());
        Job job = jobService.findById(id);
        applicationService.apply(seeker, job);
        return "redirect:/jobs";
    }

    /** Restricted to JOB_SEEKER in SecurityConfiguration. */
    @GetMapping("/applications")
    public String myApplications(Authentication authentication, Model model) {
        User seeker = userService.findByEmail(authentication.getName());
        model.addAttribute("applications", applicationService.myApplications(seeker));
        model.addAttribute("roleLabel", roleLabel(seeker));
        return "my-applications";
    }

    /** Restricted to RECRUITER in SecurityConfiguration; ownership is still checked here. */
    @GetMapping("/jobs/{id}/applicants")
    public String applicants(@PathVariable Long id, Authentication authentication, Model model) {
        User recruiter = userService.findByEmail(authentication.getName());
        Job job = jobService.findById(id);

        // A recruiter can only see applicants for jobs they posted - not every recruiter's jobs.
        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            return "redirect:/jobs/mine";
        }

        model.addAttribute("job", job);
        model.addAttribute("applicants", applicationService.applicantsFor(job));
        model.addAttribute("roleLabel", roleLabel(recruiter));
        return "job-applicants";
    }

    private String roleLabel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Admin";
            case RECRUITER -> "Recruiter";
            case JOB_SEEKER -> "Job Seeker";
        };
    }
}
