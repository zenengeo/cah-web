package me.itzg.cahweb.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DealHandRequest(
    @NotBlank String playerId,
    @Min(1) int count
) {

}
