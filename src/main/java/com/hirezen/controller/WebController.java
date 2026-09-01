package com.hirezen.controller;

import com.hirezen.model.Role;
import com.hirezen.model.StatCard;
import com.hirezen.service.EmailAlreadyExistsException;
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

    /** Only these two roles can be picked at signup - ADMIN accounts are provisioned separately. */
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

        // Defence in depth: even though the signup form only offers these two
        // roles, never trust client input - reject anything else server-side
        // (e.g. someone hand-crafting a POST with role=ADMIN).
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
    // The POST /signin submission itself is handled entirely by Spring Security's
    // formLogin filter configured in SecurityConfiguration - no controller method
    // is needed (or should exist) for it.

    @GetMapping("/signin")
    public String signinPage() {
        return "signin";
    }

    // ---------- Dashboards ----------

    /** Generic entry point after login - sends the user to the dashboard that matches their role. */
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
        // Should never happen - every account has exactly one of the roles above.
        log.warn("Authenticated user with no recognized role: {}", authentication.getName());
        return "redirect:/signin?error=true";
    }

    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("roleLabel", "Admin");
        model.addAttribute("stats", adminStatsPlaceholder());
        return "dashboard";
    }

    @GetMapping("/dashboard/recruiter")
    public String recruiterDashboard(Model model) {
        model.addAttribute("roleLabel", "Recruiter");
        model.addAttribute("stats", recruiterStatsPlaceholder());
        return "dashboard";
    }

    @GetMapping("/dashboard/job-seeker")
    public String jobSeekerDashboard(Model model) {
        model.addAttribute("roleLabel", "Job Seeker");
        model.addAttribute("stats", jobSeekerStatsPlaceholder());
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

    /** Human-readable role label for the header/nav - shared by dashboard and every Coming Soon page. */
    private String resolveRoleLabel(Authentication authentication) {
        if (hasAuthority(authentication, Role.ADMIN)) return "Admin";
        if (hasAuthority(authentication, Role.RECRUITER)) return "Recruiter";
        if (hasAuthority(authentication, Role.JOB_SEEKER)) return "Job Seeker";
        return "";
    }

    // ---------- Nav placeholders ----------
    // Every nav link must resolve to a real page - nothing disabled, nothing
    // 404ing. These stand in for features not built yet. Jobs/Connect/Pulse/
    // Profile get replaced with real controllers as each day's work lands;
    // Gigs/Campus/ZenAI stay Coming Soon all the way through the demo (see
    // the sprint plan, section 2).
    //
    // NOTE: /profile is now handled by ProfileController - the old
    // profileComingSoon() method has been removed to avoid a mapping clash.

    @GetMapping("/jobs")
    public String jobsComingSoon(Authentication authentication, Model model) {
        model.addAttribute("roleLabel", resolveRoleLabel(authentication));
        model.addAttribute("featureName", "Jobs");
        model.addAttribute("message", "Job posting and browsing lands here next - coming soon.");
        return "coming-soon";
    }

    @GetMapping("/gigs")
    public String gigsComingSoon(Authentication authentication, Model model) {
        model.addAttribute("roleLabel", resolveRoleLabel(authentication));
        model.addAttribute("featureName", "Gigs");
        model.addAttribute("message", "Post or browse skill-based gigs, Fiverr-style - coming soon.");
        return "coming-soon";
    }

    @GetMapping("/messages")
    public String connectComingSoon(Authentication authentication, Model model) {
        model.addAttribute("roleLabel", resolveRoleLabel(authentication));
        model.addAttribute("featureName", "Connect");
        model.addAttribute("message", "Direct messaging between recruiters and job seekers - coming soon.");
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
    // Real numbers will come from JobRepository / ApplicationRepository /
    // NotificationRepository once those entities exist. Each method below
    // returns the same List<StatCard> shape regardless of role, which is
    // what lets one dashboard.html template serve all three roles - the
    // template just loops over the list, it never needs to know which
    // role it's rendering for.

    private List<StatCard> adminStatsPlaceholder() {
        return List.of(
                new StatCard("Total Users", 0),
                new StatCard("Total Job Seekers", 0),
                new StatCard("Total Recruiters", 0),
                new StatCard("Total Companies", 0),
                new StatCard("Total Jobs", 0),
                new StatCard("Total Applications", 0),
                new StatCard("Active Jobs", 0)
        );
    }

    private List<StatCard> recruiterStatsPlaceholder() {
        return List.of(
                new StatCard("Total Jobs", 0),
                new StatCard("Active Jobs", 0),
                new StatCard("Total Applicants", 0),
                new StatCard("Shortlisted", 0),
                new StatCard("Rejected", 0),
                new StatCard("Upcoming Interviews", 0)
        );
    }

    private List<StatCard> jobSeekerStatsPlaceholder() {
        return List.of(
                new StatCard("Total Applications", 0),
                new StatCard("Shortlisted", 0),
                new StatCard("Interview Invitations", 0),
                new StatCard("Rejected", 0),
                new StatCard("Saved Jobs", 0)
        );
    }
}
