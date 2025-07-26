package br.com.unicat.mocks.deckofcards.adapter.repository.provider;

import br.com.unicat.mocks.deckofcards.adapter.repository.h2db.DeckRepositoryH2Db;
import br.com.unicat.mocks.deckofcards.entities.DeckEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DeckDbProvider {
    private final DeckRepositoryH2Db deckRepositoryH2Db;

    public DeckEntity save(final DeckEntity deck) {
        log.info("Saving deck in database. deck id: [{}]", deck.getId());
        final var savedDeck = deckRepositoryH2Db.save(deck);

        log.info("Deck was saved in the database with success. deck id: [{}]", savedDeck.getId());
        return savedDeck;
    }
}
