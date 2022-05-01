package me.itzg.cahweb.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public record CardSubmission(
    @Min(1)
    int cardId,
    @NotBlank
    String playerId
) {

}
