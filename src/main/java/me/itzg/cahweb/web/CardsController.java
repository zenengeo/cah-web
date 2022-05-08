package me.itzg.cahweb.web;

import javax.validation.constraints.Min;
import me.itzg.cahweb.model.ListResponse;
import me.itzg.cahweb.model.WhiteCard;
import me.itzg.cahweb.services.CardsProvider;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
@Validated
public class CardsController {

    private final CardsProvider cardsProvider;

    public CardsController(CardsProvider cardsProvider) {
        this.cardsProvider = cardsProvider;
    }

    @GetMapping("/randomNameCards")
    public ListResponse<WhiteCard> randomNameCards(@RequestParam(defaultValue = "1") @Min(1) int count) {
        return ListResponse.ofList(
            cardsProvider.getSomeWhiteCards(count, WhiteCard::useAsName)
        );
    }
}
