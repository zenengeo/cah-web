package me.itzg.cahweb.model;

import lombok.Builder;

@Builder
public record PlayerInfo(String playerId,
                         String playerName,
                         boolean ghost
) {

}
