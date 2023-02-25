package me.itzg.cahweb.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CardSubmission(
    @Min(1)
    int cardId,
    @NotBlank
    String playerId
) {

}
