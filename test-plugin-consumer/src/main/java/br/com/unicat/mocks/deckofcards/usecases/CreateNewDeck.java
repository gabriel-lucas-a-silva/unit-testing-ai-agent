package br.com.unicat.mocks.deckofcards.usecases;

import br.com.unicat.mocks.deckofcards.adapter.gateway.DeckClientProvider;
import br.com.unicat.mocks.deckofcards.adapter.http.dto.response.DeckResponseDTO;
import br.com.unicat.mocks.deckofcards.adapter.repository.provider.DeckDbProvider;
import br.com.unicat.mocks.deckofcards.entities.DeckEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateNewDeck {
    private final DeckClientProvider deckClientProvider;
    private final DeckDbProvider deckDbProvider;

    public DeckResponseDTO execute() {
        final var deck = deckClientProvider.createDeck();
        final var deckWithItsCards = deckClientProvider.drawCards(deck);
        final var savedDeck = deckDbProvider.save(deckWithItsCards);
        return DeckEntity.toResponse(savedDeck);
    }
}
