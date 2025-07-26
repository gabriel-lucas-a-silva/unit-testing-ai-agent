package br.com.unicat.mocks.deckofcards.entities;

import br.com.unicat.mocks.deckofcards.entities.enums.CardSuit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CARDS")
public class CardEntity {
    @Id
    private UUID id;

    private String code;

    private String image;

    @Column(name = "`VALUE`")
    private String value;

    @Enumerated
    private CardSuit suit;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "deck_id")
    private DeckEntity deck;
}
