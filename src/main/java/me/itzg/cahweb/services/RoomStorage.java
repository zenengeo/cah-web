package me.itzg.cahweb.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class RoomStorage {

    @Cacheable("rooms")
    public Room getRoom(String roomCode) {
        log.debug("Creating room={}", roomCode);
        return new Room(
            Sinks.many().replay().latest(),
            Sinks.many().replay().latest()
        );
    }
}
