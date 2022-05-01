package me.itzg.cahweb.model;

import javax.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;

public record WhiteCard(
    @NotBlank
    String text,
    String by
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WhiteCard)) {
            return false;
        }

        final WhiteCard whiteCard = (WhiteCard) o;

        return text.equals(whiteCard.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
