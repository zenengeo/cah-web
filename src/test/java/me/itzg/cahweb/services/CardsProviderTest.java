package me.itzg.cahweb.services;

import me.itzg.cahweb.AppProperties;
import me.itzg.cahweb.model.ExclusiveType;
import me.itzg.cahweb.model.WhiteCard;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {CardsProvider.class},
    properties = {
        "app.cards-json=classpath:test-cards.json"
    }
)
@AutoConfigureJson
@EnableConfigurationProperties(AppProperties.class)
class CardsProviderTest {
    @Autowired
    CardsProvider cardsProvider;

    @Nested
    class getAllWhiteCards {
        @Test
        void noFiltering() {
            final Collection<WhiteCard> result = cardsProvider.getWhitecards(null, 0, Integer.MAX_VALUE);
            assertThat(result)
                .extracting(WhiteCard::text)
                .containsExactlyInAnyOrder("Just text", "With author", "Some name", "Digital exclusive", "Print exclusive");
        }

        @Test
        void filterAwayDigitalExclusive() {
            final Collection<WhiteCard> result = cardsProvider.getWhitecards(List.of(ExclusiveType.digital), 0, Integer.MAX_VALUE);
            assertThat(result)
                .extracting(WhiteCard::text)
                .containsExactlyInAnyOrder("Just text", "With author", "Some name", "Print exclusive");
        }

        @Test
        void justOne() {
            final Collection<WhiteCard> result = cardsProvider.getWhitecards(null, 0, 1);
            assertThat(result)
                .extracting(WhiteCard::text)
                .containsExactlyInAnyOrder("Just text");
        }

        @Test
        void filterOffsetAndLimit() {
            final Collection<WhiteCard> result = cardsProvider.getWhitecards(List.of(ExclusiveType.digital), 3, 1);
            assertThat(result)
                .extracting(WhiteCard::text)
                .containsExactlyInAnyOrder("Print exclusive");
        }
    }

}