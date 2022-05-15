package me.itzg.plain;

import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public abstract class ImageHandlingTask extends DefaultTask {

    @Optional
    @Input
    abstract Property<String> getImageRepo();

    @Input
    abstract Property<String> getImageName();

    @Input
    abstract ListProperty<String> getTags();

    protected String calculateFullImageName() {
        return getImageRepo().isPresent() ?
            getImageRepo().get() + "/" + getImageName().get()
            : getImageName().get();
    }

    Set<String> expandImageTags() {
        final String fullImageName = calculateFullImageName();
        return getTags().get().stream()
            .map(tag -> fullImageName + ":" + tag)
            .collect(Collectors.toSet());
    }
}
