package me.itzg.cahweb.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BlackCard(
    @NotBlank String text,
    @Min(1) int slots,
    String by
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final BlackCard blackCard)) {
            return false;
        }

        return text.equals(blackCard.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
