package me.itzg.plain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.gradle.api.DefaultTask;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

@NonNullApi
public abstract class StageJarTask extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getBootJar();

    @OutputFile
    public abstract RegularFileProperty getStagedJar();

    @TaskAction
    public void stage() throws IOException {
        Files.copy(
            getBootJar().getAsFile().get().toPath(),
            getStagedJar().getAsFile().get().toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
    }
}
