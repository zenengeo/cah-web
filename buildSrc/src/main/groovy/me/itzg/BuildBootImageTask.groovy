package me.itzg

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.BuildImageResultCallback
import com.github.dockerjava.api.model.BuildResponseItem
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

public abstract class BuildBootImageTask extends DefaultTask {
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
    abstract Property<String> getImageRepo()

    @Input
    abstract Property<String> getImageName()

    @Input
    abstract ListProperty<String> getTags()

    @Input
    abstract Property<Boolean> getPull()

    @OutputFile
    abstract RegularFileProperty getBootImageInfoFile()

    @Internal
    abstract Property<DockerClientService> getDockerClientService()

    @TaskAction
    void build() throws IOException {
        final DockerClient client = dockerClientService.get().client

        def fullImageName = imageRepo.isPresent() ? imageRepo.get()+"/"+imageName.get() : imageName.get()
        def imageTags = expandImageTags()

        logger.info("Building {} with base image {} tagged with {}",
                fullImageName, baseImage.get(), tags.get());

        def cmd = client.buildImageCmd()
                .withBaseDirectory(getBootImageDirectory().get().getAsFile())
                .withDockerfile(getDockerfile().getAsFile().get())
                .withBuildArg("BASE_IMG", getBaseImage().get())
                .withBuildArg("EXPOSE_PORT", String.valueOf(getExposePort().get()))
                .withTags(imageTags)
                .withPull(pull.get())

        final String imageId = cmd.exec(new BuildImageResultCallback() {
            @Override
            void onNext(BuildResponseItem item) {
                if (item.getStream() != null && !item.getStream().isBlank()) {
                    logger.info("Progress: {}", item.getStream().trim());
                }
                super.onNext(item);
            }
        })
                .awaitImageId();

        logger.info("Built image id={}", imageId);
        project.extensions.extraProperties.with {
            set("bootImage.imageId", imageId)
            set("bootImage.imageTags", imageTags)
        }

        final ObjectMapper objectMapper = new ObjectMapper();
        final BootImageInfo bootImageInfo = new BootImageInfo(
                imageId: imageId,
                imageTags: imageTags
        );
        objectMapper.writeValue(getBootImageInfoFile().getAsFile().get(), bootImageInfo);
    }

    Set<String> expandImageTags() {
        return tags.get().collect { tag ->
            if (tag) {
                if (imageRepo.isPresent()) {
                    return "${imageRepo.get()}/${imageName.get()}:${tag}".toString()
                } else {
                    return "${imageName.get()}:${tag}".toString()
                }
            }
        }
    }

}
