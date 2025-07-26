package br.com.unicat.mocks.deckofcards.adapter.gateway;

import br.com.unicat.mocks.deckofcards.adapter.gateway.dto.DeckResponse;
import br.com.unicat.mocks.deckofcards.adapter.gateway.dto.cards.CardsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${deck.name}", url = "${deck.url}")
public interface DeckClient {
    @GetMapping(path = "/new")
    DeckResponse createDeck();

    @GetMapping(path = "/{deckId}/draw/?count={nOfCards}")
    CardsResponse drawCard(@PathVariable String deckId,
                           @PathVariable int nOfCards);
}
