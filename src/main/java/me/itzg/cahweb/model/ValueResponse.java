package me.itzg.cahweb.model;

public record ValueResponse<T>(T value) {

    public static <T> ValueResponse<T> ofValue(T value) {
        return new ValueResponse<>(value);
    }
}
