package br.com.unicat.mocks.deckofcards.adapter.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayersResponseDTO {
    private Map<String, Set<String>> players;
}
