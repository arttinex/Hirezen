package com.hirezen.controller;

import com.hirezen.model.EmploymentType;
import com.hirezen.model.Job;
import com.hirezen.model.Role;
import com.hirezen.model.User;
import com.hirezen.service.ApplicationService;
import com.hirezen.service.JobService;
import com.hirezen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final UserService userService;
    private final ApplicationService applicationService;

    /** Open to every logged-in user - the shared "browse jobs" list. */
    @GetMapping
    public String browseJobs(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName());
        model.addAttribute("jobs", jobService.openJobs());
        model.addAttribute("roleLabel", roleLabel(user));

        // Only job seekers need to know which jobs they've already applied to
        // (drives the "Applied" vs "Apply" state of the button in the template).
        if (user.getRole() == Role.JOB_SEEKER) {
            model.addAttribute("appliedJobIds", applicationService.appliedJobIds(user));
        }

        return "jobs-browse";
    }

    /** Restricted to RECRUITER in SecurityConfiguration. */
    @GetMapping("/new")
    public String newJobForm(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName());
        model.addAttribute("roleLabel", roleLabel(user));
        model.addAttribute("employmentTypes", EmploymentType.values());
        return "job-post";
    }

    @PostMapping("/new")
    public String createJob(
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String companyName,
            @RequestParam String location,
            @RequestParam EmploymentType employmentType) {

        User recruiter = userService.findByEmail(authentication.getName());
        jobService.postJob(recruiter, title, description, companyName, location, employmentType);
        return "redirect:/jobs/mine";
    }

    /** Restricted to RECRUITER in SecurityConfiguration. */
    @GetMapping("/mine")
    public String myJobs(Authentication authentication, Model model) {
        User recruiter = userService.findByEmail(authentication.getName());
        List<Job> jobs = jobService.jobsPostedBy(recruiter);

        // Applicant count per job, shown as "View Applicants (n)" on each card.
        Map<Long, Long> applicantCounts = new HashMap<>();
        for (Job job : jobs) {
            applicantCounts.put(job.getId(), applicationService.countForJob(job));
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("applicantCounts", applicantCounts);
        model.addAttribute("roleLabel", roleLabel(recruiter));
        return "jobs-mine";
    }

    private String roleLabel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Admin";
            case RECRUITER -> "Recruiter";
            case JOB_SEEKER -> "Job Seeker";
        };
    }
}
