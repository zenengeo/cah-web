package me.itzg.cahweb.web;

import jakarta.validation.constraints.Min;
import me.itzg.cahweb.model.BlackCard;
import me.itzg.cahweb.model.ExclusiveType;
import me.itzg.cahweb.model.ListResponse;
import me.itzg.cahweb.model.WhiteCard;
import me.itzg.cahweb.services.CardsProvider;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/randomWhite")
    public ListResponse<WhiteCard> randomWhiteCards(@RequestParam(defaultValue = "1") @Min(1) int count) {
        return ListResponse.ofList(
            cardsProvider.getSomeWhiteCards(count, whiteCard -> true)
        );
    }

    @GetMapping("/randomBlack")
    public ListResponse<BlackCard> randomBlackCards(@RequestParam(defaultValue = "1") @Min(1) int count) {
        return ListResponse.ofList(
            cardsProvider.getSomeBlackCards(count)
        );
    }

    @GetMapping("/whiteCards")
    public ListResponse<WhiteCard> whiteCards(@RequestParam List<ExclusiveType> excludeExclusiveTypes,
        @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "8") int count
        ) {
        return ListResponse.ofList(
            cardsProvider.getWhitecards(excludeExclusiveTypes, offset, count)
        );
    }
}
