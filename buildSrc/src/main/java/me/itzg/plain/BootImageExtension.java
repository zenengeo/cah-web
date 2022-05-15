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
}
