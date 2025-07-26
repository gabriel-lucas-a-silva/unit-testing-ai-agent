package br.com.unicat.mocks.deckofcards.adapter.repository.provider;

import br.com.unicat.mocks.deckofcards.adapter.repository.h2db.CardsRepositoryH2Db;
import br.com.unicat.mocks.deckofcards.entities.CardEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CardsDbProvider {
    private final CardsRepositoryH2Db cardsRepositoryH2Db;

    public List<CardEntity> findCardsByDeckId(final String deckId) {
        log.info("Looking for cards with deck id [{}]", deckId);
        final var cards = cardsRepositoryH2Db.findCardEntityByDeckId(deckId);

        log.info("Successfully looked for cards to deck id [{}]. Cards size: {}", deckId, cards.size());
        return cards;
    }
}
