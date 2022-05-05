package me.itzg.cahweb.model;

import java.util.List;
import javax.validation.Valid;

public record CardsSource(
    List<@Valid BlackCard> black,
    List<@Valid WhiteCard> white
) {

}
