package me.itzg.cahweb.model;

import javax.validation.constraints.NotBlank;

public record WhiteCard(
    @NotBlank
    String text,
    String by,
    boolean useAsName
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
