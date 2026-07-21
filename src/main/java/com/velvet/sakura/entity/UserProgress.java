package com.velvet.sakura.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProgress {

    @Id
    private Long accountId;

    @Column(nullable = false)
    @Builder.Default
    private int level = 1;

    @Column(nullable = false)
    @Builder.Default
    private int experience = 0;

    @Column(nullable = false)
    @Builder.Default
    private int credits = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalReadings = 0;
}