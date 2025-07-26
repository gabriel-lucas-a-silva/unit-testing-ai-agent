package br.com.unicat.mocks.deckofcards.adapter.http.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRequestDTO {
    private String name;
    private List<String> cards;
}
