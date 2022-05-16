package me.itzg.plain;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
@NonNullApi
public abstract class GenerateLayeredDockerfileTask extends DefaultTask {

    @OutputFile
    abstract RegularFileProperty getDockerfile();

    @TaskAction
    public void generate() throws IOException {
        Files.write(getDockerfile().get().getAsFile().toPath(),
            List.of(
                // ARG for base image needs a placeholder
                "ARG BASE_IMG=eclipse-temurin:17",
                "FROM ${BASE_IMG}",
                "ARG EXPOSE_PORT",
                "EXPOSE ${EXPOSE_PORT}",
                "WORKDIR /application",
                "COPY layers/dependencies/ ./",
                "COPY layers/spring-boot-loader/ ./",
                "COPY layers/snapshot-dependencies/ ./",
                "COPY layers/application/ ./",
                "ENTRYPOINT [\"java\", \"org.springframework.boot.loader.JarLauncher\"]"
            )
        );
    }
}
