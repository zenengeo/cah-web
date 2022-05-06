package me.itzg.cahweb.services;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import me.itzg.cahweb.AppProperties;
import me.itzg.cahweb.model.BlackCard;
import me.itzg.cahweb.model.CardsSource;
import me.itzg.cahweb.model.WhiteCard;
import org.springframework.stereotype.Service;

@Service
public class CardsProvider {

    private final CardsSource cardsSource;
    private final Random rand;

    public CardsProvider(AppProperties appProperties,
        ObjectMapper objectMapper
        ) throws IOException {
        try (InputStream cardsIn = appProperties.cardsJson().getInputStream()) {
            cardsSource = objectMapper.readValue(cardsIn, CardsSource.class);
        }

        rand = new Random();
    }
    
    public Collection<BlackCard> getSomeBlackCards(int count) {
        final Set<BlackCard> picked = new HashSet<>();
        while (picked.size() < count) {
            final BlackCard card = cardsSource.black().get(rand.nextInt(0, cardsSource.black().size()));

            if (card.cards() == 1) {
                // duplicate just gets skipped since it's a set
                picked.add(card);
            }
            // TODO support blackcards with 2+ slots
        }
        return picked;
    }

    public Collection<BlackCard> shuffleDeckOfBlackCards(Predicate<BlackCard> filter) {
        return cardsSource.black().stream()
            .filter(filter)
            .collect(collectingAndThen(toList(), blackCards -> {
                Collections.shuffle(blackCards);
                return blackCards;
            }));

    }

    /**
     *
     * @param validator used for things like checking if someone else got dealt this card
     */
    public Collection<WhiteCard> getSomeWhiteCards(int count,
        Predicate<WhiteCard> validator) {
        final Set<WhiteCard> picked = new HashSet<>();
        while (picked.size() < count) {
            final WhiteCard card = cardsSource.white().get(rand.nextInt(0, cardsSource.white().size()));
            if (validator.test(card)) {
                // duplicate just gets skipped since it's a set
                picked.add(card);
            }
        }
        return picked;
    }
}
