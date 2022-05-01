package me.itzg.cahweb.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotReadyForRequestException extends RuntimeException {

    public NotReadyForRequestException(String message) {
        super(message);
    }
}
