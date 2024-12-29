package me.itzg.cahweb.web;

import me.itzg.cahweb.model.ExclusiveType;
import me.itzg.cahweb.model.WhiteCard;
import me.itzg.cahweb.services.CardsProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

/**
 * Set of server-side rendered endpoints
 */
@Controller
@RequestMapping("/printables")
public class PrintablesController {

    private final CardsProvider cardsProvider;

    public PrintablesController(CardsProvider cardsProvider) {
        this.cardsProvider = cardsProvider;
    }

    @GetMapping("/whitecards")
    public String whitecards(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "8") int count, Model model) {
        final Collection<WhiteCard> cards = cardsProvider.getWhitecards(List.of(ExclusiveType.digital), offset,
            count);
        model.addAttribute("cards", cards);
        model.addAttribute("offset", offset);
        model.addAttribute("count", count);
        return "printables/whitecards";
    }
}
