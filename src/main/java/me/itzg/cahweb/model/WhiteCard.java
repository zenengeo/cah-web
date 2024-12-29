package me.itzg.cahweb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

/**
 * White cards are dealt to players and selected by them for play. Usually fills in to the slots for {@link BlackCard}s
 * @param text card/body text
 * @param by author
 * @param useAsName can be used as a ghost player name
 * @param exclusive non-null indicates this card is exclusive to the specified type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WhiteCard(
    @NotBlank
    String text,
    String by,
    boolean useAsName,
    @Nullable
    ExclusiveType exclusive
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final WhiteCard whiteCard)) {
            return false;
        }

        return text.equals(whiteCard.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
