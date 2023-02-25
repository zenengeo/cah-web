package me.itzg.cahweb.model;

import lombok.Builder;

@Builder
public record PlayerSession(
    String playerId,
    String roomCode
) {

}
