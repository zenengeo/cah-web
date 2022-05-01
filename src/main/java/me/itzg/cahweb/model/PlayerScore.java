package me.itzg.cahweb.model;

import lombok.Builder;

@Builder
public record PlayerScore(
    PlayerInfo info,
    int score
) {

}
