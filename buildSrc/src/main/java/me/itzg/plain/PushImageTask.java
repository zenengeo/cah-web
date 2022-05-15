package me.itzg.plain;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.PushResponseItem;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public abstract class PushImageTask extends ImageHandlingTask {

    @Internal
    abstract Property<DockerClientService> getDockerClientService();

    @TaskAction
    void push() throws InterruptedException {
        final DockerClient client = getDockerClientService().get().getClient();

        var fullImageName = calculateFullImageName();

        for (final String tag : getTags().get()) {
            getLogger().info("Pushing image {}:{}", fullImageName, tag);

            client.pushImageCmd(fullImageName)
                .withTag(tag)
                .exec(new ResultCallback.Adapter<PushResponseItem>() {
                    @Override
                    public void onNext(PushResponseItem item) {
                        if (item.getStream() != null && !item.getStream().isBlank()) {
                            getLogger().info("Docker push: {}", item.getStream().trim());
                        }
                        if (item.getStatus() != null) {
                            getLogger().info("Docker push: {}{}", item.getId() != null ? item.getId()+" " : "", item.getStatus());
                        }
                        super.onNext(item);
                    }
                })
                .awaitCompletion();

        }
    }

}
