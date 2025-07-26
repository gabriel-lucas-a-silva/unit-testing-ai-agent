package br.com.unicat.mocks.deckofcards.usecases;

import br.com.unicat.mocks.deckofcards.adapter.http.dto.request.PlayerRequestDTO;
import br.com.unicat.mocks.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO;
import br.com.unicat.mocks.deckofcards.entities.enums.Points;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
public class GetWinner {


    public WinnerPlayerResponseDTO execute(final List<PlayerRequestDTO> players) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        var playersPoints = new LinkedHashMap<String, Integer>();

        players.forEach(player -> {
            AtomicInteger sum = new AtomicInteger();
            player.getCards().forEach(card -> {
                var points = 0;

                try {
                    points += Integer.parseInt(card);
                } catch (NumberFormatException e) {
                    try {
                        points += Points.valueOf(card).getPoint();
                    } catch (IllegalArgumentException ex) {
                        // Ignora cartas inválidas
                        points += 0;
                    }
                }

                sum.addAndGet(points);
            });

            playersPoints.put(player.getName(), sum.get());
        });

        String winner = getPlayerWithHighestPoints(playersPoints);
        if (winner == null) {
            return null;
        }
        
        Integer points = playersPoints.get(winner);
        return new WinnerPlayerResponseDTO(winner, points != null ? points : 0);
    }

    private <K, V extends Comparable<V>> K getPlayerWithHighestPoints(Map<K, V> playersPoints) {
        V maxValue = null;
        K winner = null;
        for (Map.Entry<K, V> entry : playersPoints.entrySet()) {
            if (maxValue == null || entry.getValue().compareTo(maxValue) > 0) {
                maxValue = entry.getValue();
                winner = entry.getKey();
            }
        }
        return winner;
    }
}
