package me.itzg.cahweb.model;

import java.util.List;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;

public record CardsSource(
    List<@Valid BlackCard> black,
    List<@Valid WhiteCard> white
) {

}
