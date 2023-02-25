package me.itzg.cahweb.model;

import jakarta.validation.constraints.NotBlank;

public record JoinRequest(
    @NotBlank String playerName
) {

}
