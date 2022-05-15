package me.itzg.plain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class BuildImageTask extends DefaultTask {

    @InputDirectory
    abstract DirectoryProperty getBootImageDirectory();

    @InputDirectory
    abstract DirectoryProperty getLayersDirectory();

    @InputFile
    abstract RegularFileProperty getDockerfile();

    @Input
    abstract Property<String> getBaseImage();

    @Input
    abstract Property<Integer> getExposePort();

    @Optional
    @Input
    abstract Property<String> getImageRepo();

    @Input
    abstract Property<String> getImageName();

    @Input
    abstract ListProperty<String> getTags();

    @Input
    abstract Property<Boolean> getPullForBuild();

    @OutputFile
    abstract RegularFileProperty getBootImageInfoFile();

    @Internal
    abstract Property<DockerClientService> getDockerClientService();

    @TaskAction
    void build() throws IOException {
        final DockerClient client = getDockerClientService().get().getClient();

        var fullImageName = getImageRepo().isPresent() ? getImageRepo().get() + "/" + getImageName().get() : getImageName().get();
        var imageTags = expandImageTags();

        getLogger().info("Building {} with base image {} tagged with {}",
            fullImageName, getBaseImage().get(), getTags().get());

        try (Stream<Path> pathStream = Files.walk(getBootImageDirectory().get().getAsFile().toPath())) {
            pathStream
                .forEach(path -> getLogger().info("Context: {}{}", path, Files.isDirectory(path) ? "/" : ""));
        }

        var cmd = client.buildImageCmd()
            .withBaseDirectory(getBootImageDirectory().get().getAsFile())
            .withDockerfile(getDockerfile().getAsFile().get())
            .withBuildArg("BASE_IMG", getBaseImage().get())
            .withBuildArg("EXPOSE_PORT", String.valueOf(getExposePort().get()))
            .withTags(imageTags)
            .withPull(getPullForBuild().get());

        final String imageId = cmd.exec(new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    if (item.getStream() != null && !item.getStream().isBlank()) {
                        getLogger().info("Docker build: {}", item.getStream().trim());
                    }
                    super.onNext(item);
                }
            })
            .awaitImageId();

        getLogger().info("Built image id {}", imageId);
        getProject().getExtensions().getExtraProperties().set("plainBootImage.imageId", imageId);

        final ObjectMapper objectMapper = new ObjectMapper();
        final BootImageInfo bootImageInfo = new BootImageInfo()
            .setImageId(imageId);
        objectMapper.writeValue(getBootImageInfoFile().getAsFile().get(), bootImageInfo);
    }

    Set<String> expandImageTags() {
        return getTags().get().stream()
            .map(tag ->
                getImageRepo().isPresent() ?
                    getImageRepo().get() + "/" + getImageName().get() + ":" + tag
                    : getImageName().get() + ":" + tag)
            .collect(Collectors.toSet());
    }

}
