package me.itzg

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.PushResponseItem
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class PushBootImageTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getBootImageInfoFile()

    @Optional
    @Input
    abstract Property<String> getImageRepo()

    @Input
    abstract Property<String> getImageName()

    @Input
    abstract ListProperty<String> getTags()

    @Internal
    abstract Property<DockerClientService> getDockerClientService()

    @TaskAction
    void push() {
        final DockerClient client = dockerClientService.get().client

        def fullImageName = imageRepo.isPresent() ? imageRepo.get()+"/"+imageName.get() : imageName.get()

        logger.info('Pushing {} with tags {}', fullImageName, tags.get())

        for (final def tag in tags.get()) {
            client.pushImageCmd(fullImageName)
                    .withTag(tag)
                    .exec(new ResultCallback.Adapter<PushResponseItem>() {
                        @Override
                        void onNext(PushResponseItem resp) {
                            if (!resp.stream?.isBlank()) {
                                logger.info('Push: {}', resp.stream.trim())
                            }
                            super.onNext(resp)
                        }
                    })


        }

    }
}
