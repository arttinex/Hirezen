package com.hirezen.controller;

import com.hirezen.model.Role;
import com.hirezen.model.StatCard;
import com.hirezen.model.User;
import com.hirezen.service.ApplicationService;
import com.hirezen.service.EmailAlreadyExistsException;
import com.hirezen.service.JobService;
import com.hirezen.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    private static final Role[] SELF_REGISTERABLE_ROLES = { Role.JOB_SEEKER, Role.RECRUITER };

    @ModelAttribute("selfRegisterableRoles")
    public Role[] selfRegisterableRoles() {
        return SELF_REGISTERABLE_ROLES;
    }

    @GetMapping("/")
    public String homePage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/signin";
    }

    // ---------- Sign up ----------

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            Model model) {

        Role parsedRole;
        try {
            parsedRole = Role.valueOf(role);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", "Please choose a valid role.");
            return "signup";
        }

        if (parsedRole != Role.JOB_SEEKER && parsedRole != Role.RECRUITER) {
            model.addAttribute("error", "Please choose a valid role.");
            return "signup";
        }

        try {
            userService.signup(name, email, password, parsedRole);
        } catch (EmailAlreadyExistsException ex) {
            model.addAttribute("error", ex.getMessage());
            return "signup";
        }

        return "redirect:/signin?registered=true";
    }

    // ---------- Sign in ----------

    @GetMapping("/signin")
    public String signinPage() {
        return "signin";
    }

    // ---------- Dashboards ----------

    @GetMapping("/dashboard")
    public String dashboardRouter(Authentication authentication) {
        if (hasAuthority(authentication, Role.ADMIN)) {
            return "redirect:/dashboard/admin";
        }
        if (hasAuthority(authentication, Role.RECRUITER)) {
            return "redirect:/dashboard/recruiter";
        }
        if (hasAuthority(authentication, Role.JOB_SEEKER)) {
            return "redirect:/dashboard/job-seeker";
        }
        log.warn("Authenticated user with no recognized role: {}", authentication.getName());
        return "redirect:/signin?error=true";
    }

    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("roleLabel", "Admin");
        model.addAttribute("stats", adminStats());
        return "dashboard";
    }

    @GetMapping("/dashboard/recruiter")
    public String recruiterDashboard(Authentication authentication, Model model) {
        User recruiter = userService.findByEmail(authentication.getName());
        model.addAttribute("roleLabel", "Recruiter");
        model.addAttribute("stats", recruiterStats(recruiter));
        return "dashboard";
    }

    @GetMapping("/dashboard/job-seeker")
    public String jobSeekerDashboard(Authentication authentication, Model model) {
        User seeker = userService.findByEmail(authentication.getName());
        model.addAttribute("roleLabel", "Job Seeker");
        model.addAttribute("stats", jobSeekerStats(seeker));
        return "dashboard";
    }

    private boolean hasAuthority(Authentication authentication, Role role) {
        if (authentication == null) return false;
        String target = role.authority();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (target.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private String resolveRoleLabel(Authentication authentication) {
        if (hasAuthority(authentication, Role.ADMIN)) return "Admin";
        if (hasAuthority(authentication, Role.RECRUITER)) return "Recruiter";
        if (hasAuthority(authentication, Role.JOB_SEEKER)) return "Job Seeker";
        return "";
    }

    // ---------- Nav placeholders ----------
    // /jobs -> JobController, /profile -> ProfileController, /search -> SearchController,
    // /messages -> MessageController. Gigs/Pulse/Campus/ZenAI remain Coming Soon.

    @GetMapping("/gigs")
    public String gigsComingSoon(Authentication authentication, Model model) {
        model.addAttribute("roleLabel", resolveRoleLabel(authentication));
        model.addAttribute("featureName", "Gigs");
        model.addAttribute("message", "Post or browse skill-based gigs, Fiverr-style - coming soon.");
        return "coming-soon";
    }

    @GetMapping("/pulse")
    public String pulseComingSoon(Authentication authentication, Model model) {
        model.addAttribute("roleLabel", resolveRoleLabel(authentication));
        model.addAttribute("featureName", "Pulse");
        model.addAttribute("message", "Notifications for applications and messages - coming soon.");
        return "coming-soon";
    }

    @GetMapping("/campus")
    public String campusComingSoon(Authentication authentication, Model model) {
        model.addAttribute("roleLabel", resolveRoleLabel(authentication));
        model.addAttribute("featureName", "Campus");
        model.addAttribute("message", "A community feed is on the roadmap. For now, check out this blog:");
        model.addAttribute("externalLinkUrl", "https://spring.io/blog");
        model.addAttribute("externalLinkLabel", "Visit the Spring Blog");
        return "coming-soon";
    }

    @GetMapping("/zenai")
    public String zenAiComingSoon(Authentication authentication, Model model) {
        model.addAttribute("roleLabel", resolveRoleLabel(authentication));
        model.addAttribute("featureName", "ZenAI");
        model.addAttribute("message", "AI-powered recommendations - coming soon.");
        return "coming-soon";
    }

    // ---------- Dashboard stat-list builders ----------

    private List<StatCard> adminStats() {
        return List.of(
                new StatCard("Total Users", userService.totalUsersCount()),
                new StatCard("Total Job Seekers", userService.countByRole(Role.JOB_SEEKER)),
                new StatCard("Total Recruiters", userService.countByRole(Role.RECRUITER)),
                new StatCard("Total Companies", jobService.distinctCompanyCount()),
                new StatCard("Total Jobs", jobService.totalJobsCount()),
                new StatCard("Total Applications", applicationService.totalApplicationsCount()),
                new StatCard("Active Jobs", jobService.totalOpenJobs())
        );
    }

    private List<StatCard> recruiterStats(User recruiter) {
        return List.of(
                new StatCard("Total Jobs", jobService.totalJobsBy(recruiter)),
                new StatCard("Active Jobs", jobService.openJobsCountBy(recruiter)),
                new StatCard("Total Applicants", applicationService.applicantsCountForRecruiter(recruiter)),
                new StatCard("Shortlisted", applicationService.shortlistedCountForRecruiter(recruiter)),
                new StatCard("Rejected", applicationService.rejectedCountForRecruiter(recruiter)),
                new StatCard("Upcoming Interviews", 0) // not built yet - no interview scheduling feature
        );
    }

    private List<StatCard> jobSeekerStats(User seeker) {
        return List.of(
                new StatCard("Total Applications", applicationService.countForSeeker(seeker)),
                new StatCard("Shortlisted", applicationService.shortlistedCountForSeeker(seeker)),
                new StatCard("Interview Invitations", 0), // not built yet - no interview scheduling feature
                new StatCard("Rejected", applicationService.rejectedCountForSeeker(seeker)),
                new StatCard("Saved Jobs", 0) // not built yet - no save-for-later feature
        );
    }
}
