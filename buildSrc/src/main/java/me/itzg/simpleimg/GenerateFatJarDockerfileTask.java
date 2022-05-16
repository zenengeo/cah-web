package me.itzg.simpleimg;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
@NonNullApi
public abstract class GenerateFatJarDockerfileTask extends DefaultTask {

    @InputFile
    @PathSensitive(value = PathSensitivity.RELATIVE)
    abstract RegularFileProperty getStagedJar();

    @OutputFile
    abstract RegularFileProperty getDockerfile();

    @TaskAction
    public void generate() throws IOException {
        final String bootJarFilename = getStagedJar().get().getAsFile().getName();

        Files.write(getDockerfile().get().getAsFile().toPath(),
            List.of(
                // ARG for base image needs a placeholder
                "ARG BASE_IMG=eclipse-temurin:17",
                "FROM ${BASE_IMG}",
                "ARG EXPOSE_PORT",
                "EXPOSE ${EXPOSE_PORT}",
                "WORKDIR /application",
                "COPY " + bootJarFilename + " ./",
                "ENTRYPOINT [\"java\", \"-jar\", \"" + bootJarFilename + "\"]"
            )
        );
    }
}
