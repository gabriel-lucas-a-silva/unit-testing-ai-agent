package br.com.unicat.mocks.deckofcards.adapter.http;

import br.com.unicat.mocks.deckofcards.adapter.http.dto.request.PlayerRequestDTO;
import br.com.unicat.mocks.deckofcards.adapter.http.dto.response.DeckResponseDTO;
import br.com.unicat.mocks.deckofcards.adapter.http.dto.response.PlayersResponseDTO;
import br.com.unicat.mocks.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO;
import br.com.unicat.mocks.deckofcards.usecases.BuildHands;
import br.com.unicat.mocks.deckofcards.usecases.CreateNewDeck;
import br.com.unicat.mocks.deckofcards.usecases.GetWinner;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/v1/api/deck")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DeckController {
    private final CreateNewDeck createNewDeck;
    private final BuildHands buildHands;
    private GetWinner getWinner;

    @GetMapping(path = "/create")
    public ResponseEntity<DeckResponseDTO> createDeck() {
        log.info("INIT createDeck.");
        final var createdDeck = createNewDeck.execute();

        log.info("SUCCESS createDeck. created deck: {}", createdDeck);
        return ResponseEntity.status(CREATED).body(createdDeck);
    }

    @GetMapping(path = "/build-hands/{deckId}")
    public ResponseEntity<PlayersResponseDTO> buildHands(@PathVariable final String deckId) {
        log.info("INIT buildHands. deck id: {}", deckId);
        final var playersHands = buildHands.execute(deckId);

        log.info("SUCCESS buildHands.");
        return ResponseEntity.status(CREATED).body(playersHands);
    }

    @PostMapping(path = "/winner/{deckId}")
    public ResponseEntity<WinnerPlayerResponseDTO> getWinnerFromDeck(@PathVariable final String deckId,
                                                                     @RequestBody final List<PlayerRequestDTO> players) {
        log.info("INIT getWinnerFromDeck.");
        final var winner = getWinner.execute(players);

        log.info("SUCCESS getWinnerFromDeck.");
        return ResponseEntity.status(OK).body(winner);
    }
}
