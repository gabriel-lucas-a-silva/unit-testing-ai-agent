package br.com.unicat.mocks.deckofcards.adapter.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WinnerPlayerResponseDTO {
    private String name;
    private int points;
}
