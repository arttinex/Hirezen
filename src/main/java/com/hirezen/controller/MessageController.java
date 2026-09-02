package com.hirezen.controller;

import com.hirezen.model.ConversationSummary;
import com.hirezen.model.User;
import com.hirezen.service.MessageService;
import com.hirezen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @GetMapping
    public String inbox(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName());
        var conversations = messageService.conversationsFor(user);

        model.addAttribute("primary", conversations.stream().filter(ConversationSummary::primary).toList());
        model.addAttribute("general", conversations.stream().filter(c -> !c.primary()).toList());
        model.addAttribute("roleLabel", roleLabel(user));
        return "messages-inbox";
    }

    @GetMapping("/{userId}")
    public String thread(@PathVariable Long userId, Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName());
        User partner = userService.findById(userId);

        if (partner.getId().equals(user.getId())) {
            return "redirect:/messages"; // can't message yourself
        }

        messageService.markAsRead(user, partner);

        model.addAttribute("partner", partner);
        model.addAttribute("thread", messageService.conversationWith(user, partner));
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("roleLabel", roleLabel(user));
        return "messages-thread";
    }

    @PostMapping("/{userId}")
    public String sendMessage(@PathVariable Long userId, @RequestParam String content, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        User partner = userService.findById(userId);

        if (!partner.getId().equals(user.getId()) && content != null && !content.isBlank()) {
            messageService.send(user, partner, content);
        }

        return "redirect:/messages/" + userId;
    }

    private String roleLabel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Admin";
            case RECRUITER -> "Recruiter";
            case JOB_SEEKER -> "Job Seeker";
        };
    }
}
