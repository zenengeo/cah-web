package me.itzg.simpleimg;

import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class BootImageExtension {

    void apply(Project project) {
        getBaseImage().convention("eclipse-temurin:17");
        getImageName().convention(project.getName());
        getExposePort().convention(8080);
        getTags().convention(List.of("latest"));
        getUseBuildx().convention(true);
        getPullForBuild().convention(false);
        getPush().convention(false);
        getLayered().convention(true);
    }

    abstract Property<String> getBaseImage();

    abstract Property<Integer> getExposePort();

    /**
     * Intended for build systems that supply the full repo/name:tag identifier
     */
    abstract Property<String> getFullyQualifiedImageName();

    abstract Property<String> getImageRepo();

    abstract Property<String> getImageName();

    abstract ListProperty<String> getTags();

    abstract Property<Boolean> getUseBuildx();

    abstract Property<Boolean> getPullForBuild();

    abstract Property<String> getCacheFrom();

    abstract Property<String> getCacheTo();

    abstract Property<Boolean> getPush();

    /**
     * Indicates if the built image should use Spring Boot's layertools and index or just
     * bundle and execute the jar as-is.
     */
    abstract Property<Boolean> getLayered();
}
