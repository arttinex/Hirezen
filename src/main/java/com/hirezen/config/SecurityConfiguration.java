package com.hirezen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        // Uploaded profile photos - public so <img> tags load without a session cookie issue.
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/", "/signup", "/signin").permitAll()
                        .requestMatchers("/dashboard/admin/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard/recruiter/**").hasRole("RECRUITER")
                        .requestMatchers("/dashboard/job-seeker/**").hasRole("JOB_SEEKER")
                        .requestMatchers("/dashboard").authenticated()
                        // Posting/managing jobs and viewing applicants is recruiter-only.
                        .requestMatchers("/jobs/new", "/jobs/mine", "/jobs/*/applicants").hasRole("RECRUITER")
                        // Applying and viewing your own applications is seeker-only.
                        .requestMatchers("/jobs/*/apply", "/applications").hasRole("JOB_SEEKER")
                        // Browsing (/jobs itself) stays open to any authenticated user.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/signin")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .failureUrl("/signin?error=true")
                        .defaultSuccessUrl("/dashboard", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/signin?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
