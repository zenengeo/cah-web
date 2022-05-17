package me.itzg.cahweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.metrics.jfr.FlightRecorderApplicationStartup;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
public class CahWebApplication {

    public static void main(String[] args) {
        final SpringApplication application = new SpringApplication(CahWebApplication.class);
        application.setApplicationStartup(new FlightRecorderApplicationStartup());
        application.run(args);
    }

}
