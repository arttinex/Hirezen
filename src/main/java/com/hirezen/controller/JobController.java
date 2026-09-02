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

        Map<Long, Long> applicantCounts = new HashMap<>();
        for (Job job : jobs) {
            applicantCounts.put(job.getId(), applicationService.countForJob(job));
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("applicantCounts", applicantCounts);
        model.addAttribute("roleLabel", roleLabel(recruiter));
        return "jobs-mine";
    }

    /** Restricted to RECRUITER in SecurityConfiguration; ownership is still checked here. */
    @GetMapping("/{id}/edit")
    public String editJobForm(@PathVariable Long id, Authentication authentication, Model model) {
        User recruiter = userService.findByEmail(authentication.getName());
        Job job = jobService.findById(id);

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            return "redirect:/jobs/mine";
        }

        model.addAttribute("job", job);
        model.addAttribute("employmentTypes", EmploymentType.values());
        model.addAttribute("roleLabel", roleLabel(recruiter));
        return "job-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateJob(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String companyName,
            @RequestParam String location,
            @RequestParam EmploymentType employmentType) {

        User recruiter = userService.findByEmail(authentication.getName());
        Job job = jobService.findById(id);

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            return "redirect:/jobs/mine";
        }

        jobService.updateJob(job, title, description, companyName, location, employmentType);
        return "redirect:/jobs/mine";
    }

    /** Deletes the job and any applications against it (a job can't be deleted while applications still reference it). */
    @PostMapping("/{id}/delete")
    public String deleteJob(@PathVariable Long id, Authentication authentication) {
        User recruiter = userService.findByEmail(authentication.getName());
        Job job = jobService.findById(id);

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            return "redirect:/jobs/mine";
        }

        applicationService.deleteAllForJob(job);
        jobService.deleteJob(job);
        return "redirect:/jobs/mine";
    }

    @PostMapping("/{id}/hire")
    public String markHired(@PathVariable Long id, Authentication authentication) {
        User recruiter = userService.findByEmail(authentication.getName());
        Job job = jobService.findById(id);

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            return "redirect:/jobs/mine";
        }

        jobService.markAsHired(job);
        return "redirect:/jobs/mine";
    }

    private String roleLabel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Admin";
            case RECRUITER -> "Recruiter";
            case JOB_SEEKER -> "Job Seeker";
        };
    }
}
