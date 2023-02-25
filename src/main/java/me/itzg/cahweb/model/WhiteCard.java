package me.itzg.cahweb.model;

import jakarta.validation.constraints.NotBlank;

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
