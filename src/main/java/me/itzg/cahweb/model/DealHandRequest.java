package me.itzg.cahweb.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public record DealHandRequest(
    @NotBlank String playerId,
    @Min(1) int count
) {

}
