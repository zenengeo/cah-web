package me.itzg.cahweb.model;

import lombok.Builder;

@Builder
public record DealtCard(
    int id,
    WhiteCard card
) {

}
