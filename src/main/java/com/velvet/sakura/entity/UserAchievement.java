package com.velvet.sakura.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements", uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "achievement_code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "achievement_code", nullable = false)
    private String achievementCode;

    @Column(nullable = false)
    private LocalDateTime unlockedAt;
}