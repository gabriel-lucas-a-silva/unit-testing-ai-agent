package br.com.unicat.mocks.deckofcards.adapter.gateway.dto.cards;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private String code;
    private String image;
    private String value;
    private String suit;
}
