package me.itzg.cahweb.model;

import javax.validation.constraints.NotBlank;

public record JoinRequest(
    @NotBlank String playerName
) {

}
