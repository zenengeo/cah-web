package me.itzg.plain;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.PushResponseItem;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

public abstract class PushImageTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getBootImageInfoFile();

    @Optional
    @Input
    abstract Property<String> getImageRepo();

    @Input
    abstract Property<String> getImageName();

    @Input
    abstract ListProperty<String> getTags();

    @Internal
    abstract Property<DockerClientService> getDockerClientService();

    @TaskAction
    void push() throws InterruptedException {
        final DockerClient client = getDockerClientService().get().getClient();

        var fullImageName = getImageRepo().isPresent() ? getImageRepo().get() + "/" + getImageName().get() : getImageName().get();

        getLogger().info("Pushing {} with tags {}", fullImageName, getTags().get());

        for (final String tag : getTags().get()) {
            client.pushImageCmd(fullImageName)
                .withTag(tag)
                .exec(new ResultCallback.Adapter<PushResponseItem>() {
                    @Override
                    public void onNext(PushResponseItem resp) {
                        if (resp.getStream() != null && !resp.getStream().isBlank()) {
                            getLogger().info("Push: {}", resp.getStream().trim());
                        }
                        super.onNext(resp);
                    }
                })
                .awaitCompletion();

        }
    }
}
