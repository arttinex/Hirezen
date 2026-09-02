package com.hirezen.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 1000)
    private String bio;

    private String phone;

    private String location;

    /** Comma-separated for now - simplest thing that works; a proper skills table is Phase 2. */
    @Column(length = 500)
    private String skills;

    /** Public URL path to the uploaded photo, e.g. /uploads/profile-images/user-3-1699999999.jpg */
    private String imageUrl;
}
