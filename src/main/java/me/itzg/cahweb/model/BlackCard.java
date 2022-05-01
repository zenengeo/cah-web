package me.itzg.cahweb.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public record BlackCard(
    @NotBlank String text,
    @Min(1) int cards,
    String by
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlackCard)) {
            return false;
        }

        final BlackCard blackCard = (BlackCard) o;

        return text.equals(blackCard.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
