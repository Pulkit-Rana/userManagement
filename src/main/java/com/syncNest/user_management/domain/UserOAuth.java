package com.syncNest.user_management.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_oauth")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserOAuth extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String provider; // e.g., GOOGLE, APPLE, GITHUB

    @Column(nullable = false)
    private String providerUserId; // Google sub / Apple sub etc.

    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private String tokenExpiry; // store expiry timestamp
}
