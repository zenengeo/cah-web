package me.itzg.cahweb.services;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.itzg.cahweb.AppProperties;
import me.itzg.cahweb.model.BlackCard;
import me.itzg.cahweb.model.CardsSource;
import me.itzg.cahweb.model.WhiteCard;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Service
public class CardsProvider {

    private final CardsSource cardsSource;
    private final Random rand;

    public CardsProvider(AppProperties appProperties,
        ObjectMapper objectMapper
        ) throws IOException {
        try (InputStream cardsIn = appProperties.cardsJson().getInputStream()) {
            cardsSource = objectMapper
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())
                .readValue(cardsIn, CardsSource.class);
        } catch (JsonMappingException e) {
            throw new IllegalStateException("Failed to parse cards json", e);
        }

        rand = new Random();
    }
    
    public Collection<BlackCard> getSomeBlackCards(int count) {
        final Set<BlackCard> picked = new HashSet<>();
        while (picked.size() < count) {
            final BlackCard card = cardsSource.black().get(rand.nextInt(0, cardsSource.black().size()));

            if (card.slots() == 1) {
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
