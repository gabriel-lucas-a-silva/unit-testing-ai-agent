package br.com.unicat.mocks.deckofcards.adapter.gateway.dto.cards;

import br.com.unicat.mocks.deckofcards.entities.CardEntity;
import br.com.unicat.mocks.deckofcards.entities.DeckEntity;
import br.com.unicat.mocks.deckofcards.entities.enums.CardSuit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardsResponse {
    private List<CardResponse> cards;

    public static List<CardEntity> toEntity(final CardsResponse cards, final DeckEntity deck) {
         return cards.getCards()
                 .stream()
                 .map(card ->
                     CardEntity.builder()
                             .id(UUID.randomUUID())
                             .code(card.getCode())
                             .image(card.getImage())
                             .value(card.getValue())
                             .suit(CardSuit.valueOf(card.getSuit()))
                             .deck(deck)
                             .build()
                 )
                 .collect(Collectors.toList());
    }
}
