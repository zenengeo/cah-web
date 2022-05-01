package me.itzg.cahweb.model;

import lombok.Builder;

@Builder
public record WinningCard(
    DealtCard card,
    long votes,
    PlayerInfo playedBy
) {

}
