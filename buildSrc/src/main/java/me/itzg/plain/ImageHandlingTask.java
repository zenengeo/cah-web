package me.itzg.plain;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    void apply(BootImageExtension extension) {
        if (extension.getFullyQualifiedImageName().isPresent()) {
            final Pattern namePattern = Pattern.compile("(.*)/(.*?)(:(.*))?");
            final Matcher matcher = namePattern.matcher(extension.getFullyQualifiedImageName().get());
            if (matcher.matches()) {
                getImageRepo().set(matcher.group(1));
                getImageName().set(matcher.group(2));
                getTags().set(matcher.group(3) != null ?
                    List.of(matcher.group(4)) : List.of("latest"));
            }
            else {
                throw new IllegalArgumentException("Malformed fullyQualifiedImageName");
            }
        }
        else {
            getImageRepo().set(extension.getImageRepo());
            getImageName().set(extension.getImageName());
            getTags().set(extension.getTags());
        }
    }
}
