package me.itzg.cahweb.model;

import java.util.Collection;
import java.util.List;

public record ListResponse<T>(List<T> contents) {

    public static <T> ListResponse<T> ofList(List<T> data) {
        return new ListResponse<>(data);
    }

    public static <T> ListResponse<T> ofList(Collection<T> data) {
        return new ListResponse<>(List.copyOf(data));
    }
}
