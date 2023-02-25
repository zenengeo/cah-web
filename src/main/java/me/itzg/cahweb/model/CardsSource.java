package me.itzg.cahweb.model;

import jakarta.validation.Valid;
import java.util.List;

public record CardsSource(
    List<@Valid BlackCard> black,
    List<@Valid WhiteCard> white
) {

}
