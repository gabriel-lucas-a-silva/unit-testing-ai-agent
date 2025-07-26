package br.com.unicat.mocks.deckofcards.adapter.gateway;

import br.com.unicat.mocks.deckofcards.adapter.gateway.dto.DeckResponse;
import br.com.unicat.mocks.deckofcards.adapter.gateway.dto.cards.CardsResponse;
import br.com.unicat.mocks.deckofcards.entities.DeckEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DeckClientProvider {
    private final static int NUMBER_OF_CARDS = 20;
    private final DeckClient deckClient;

    public DeckEntity createDeck() {
        log.info("Creating a new deck.");
        final var deck = deckClient.createDeck();

        log.info("New deck created with success. deck id: [{}]", deck.getDeckId());
        return DeckResponse.toEntity(deck);
    }

    public DeckEntity drawCards(final DeckEntity deck) {
        log.info("Drawing cards to deck id: [{}]", deck.getId());
        final var cards = deckClient.drawCard(deck.getId(), NUMBER_OF_CARDS);
        final var cardsAsEntities = CardsResponse.toEntity(cards, deck);

        log.info("Cards drawn with success.");
        return DeckResponse.toEntity(deck, cardsAsEntities);
    }
}
