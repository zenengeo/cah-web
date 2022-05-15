package me.itzg.plain;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class BootImageExtension {

    abstract Property<String> getBaseImage();

    abstract Property<Integer> getExposePort();

    abstract Property<String> getImageRepo();

    abstract Property<String> getImageName();

    abstract ListProperty<String> getTags();

    abstract Property<Boolean> getPullForBuild();

    abstract Property<Boolean> getPush();

    /**
     * Indicates if the built image should use Spring Boot's layertools and index or just
     * bundle and execute the jar as-is.
     */
    abstract Property<Boolean> getLayered();
}
