package com.javaworld.data;

import com.javaworld.core.jwentities.Player;
import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

import java.util.List;

@Serializable
@AllArgsConstructor
public class PlayerScoreUpdateData extends PlayerScoreUpdateSerializer {
    public final String[] playerNames;
    public final int[] playerScore;

    public PlayerScoreUpdateData(List<Player> playerScores) {
        playerNames = new String[playerScores.size()];
        playerScore = new int[playerScores.size()];

        for (int i = 0; i < playerScores.size(); i++) {
            playerNames[i] = playerScores.get(i).getName();
            playerScore[i] = playerScores.get(i).getScore();
        }
    }
}
