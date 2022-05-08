package me.itzg.cahweb.web;

import me.itzg.cahweb.model.WhiteCard;
import me.itzg.cahweb.services.CardsProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
public class CardsController {

    private final CardsProvider cardsProvider;

    public CardsController(CardsProvider cardsProvider) {
        this.cardsProvider = cardsProvider;
    }

    @GetMapping("/randomNameCard")
    public WhiteCard randomNameCard() {
        return cardsProvider.getSomeWhiteCards(1, WhiteCard::useAsName).iterator().next();
    }
}
