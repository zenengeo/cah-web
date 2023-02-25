package me.itzg.cahweb;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app")
@Validated
public record AppProperties(
    @NotNull
    @DefaultValue("classpath:cards.json")
    Resource cardsJson,

    boolean disablePlayerSessions
) {
}
