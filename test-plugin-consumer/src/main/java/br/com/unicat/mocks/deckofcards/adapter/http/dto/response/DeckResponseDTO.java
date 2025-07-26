package br.com.unicat.mocks.deckofcards.adapter.http.dto.response;

import br.com.unicat.mocks.deckofcards.adapter.gateway.dto.cards.CardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckResponseDTO {
    private String deckId;
    private List<CardResponse> cards;
}
