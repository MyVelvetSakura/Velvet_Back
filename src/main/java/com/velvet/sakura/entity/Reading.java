package com.velvet.sakura.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "readings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long pastCardId;

    @Column(nullable = false)
    private Long presentCardId;

    @Column(nullable = false)
    private Long futureCardId;

    @Enumerated(EnumType.STRING)
    @Column(name="deck_type",nullable = false)
    private DeckType deckType;

}
