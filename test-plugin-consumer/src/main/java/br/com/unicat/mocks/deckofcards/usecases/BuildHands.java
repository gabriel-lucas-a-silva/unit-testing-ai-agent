package br.com.unicat.mocks.deckofcards.usecases;

import br.com.unicat.mocks.deckofcards.adapter.http.dto.response.PlayersResponseDTO;
import br.com.unicat.mocks.deckofcards.adapter.repository.provider.CardsDbProvider;
import br.com.unicat.mocks.deckofcards.entities.CardEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class BuildHands {
    private final CardsDbProvider cardsDbProvider;

    public PlayersResponseDTO execute(final String deckId) {
        final var cards = cardsDbProvider.findCardsByDeckId(deckId);

        log.info("Building players' hand.");
        var playersCards = new HashSet<String>();
        var players = new HashMap<String, Set<String>>();
        var playerNumber = 4;

        extracted(cards, players, playerNumber, playersCards);

        log.info("Players' hands built. {}", players);
        return new PlayersResponseDTO(players);
    }

    private static void extracted(List<CardEntity> cards, HashMap<String, Set<String>> players, int playerNumber, HashSet<String> playersCards) {
        for (int i = 1; i <= cards.size(); i++) {
            final var card = cards.get(i - 1);

            if (isaBoolean(i)) {
                players.put(String.format("Jogador %s", playerNumber), playersCards);
                log.info("Built player {}. hand: {}", playerNumber, playersCards);
                playersCards = new HashSet<>();
                playerNumber--;
            }

            playersCards.add(card.getValue());
        }
    }

    private static boolean isaBoolean(int i) {
        return i % 5 == 0;
    }
}
