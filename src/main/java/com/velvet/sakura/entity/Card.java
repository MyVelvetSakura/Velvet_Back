package com.velvet.sakura.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="cards",uniqueConstraints=@UniqueConstraint(columnNames={"code","deck_type"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String code;

    @Column (nullable = false)
    private String spanishName;

    @Column (columnDefinition = "TEXT")
    private String meaning;

    private String cardImageUrl;
    private String reverseImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "deck_type", nullable = false)
    private DeckType deckType;

}
