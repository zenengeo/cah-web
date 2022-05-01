package me.itzg.cahweb.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameEvent {
    Action action;

    public Action getAction() {
        return action;
    }
}
