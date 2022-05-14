package me.itzg

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class BootImageExtension {
    abstract Property<String> getBaseImage()

    abstract Property<Integer> getExposePort()

    abstract Property<String> getImageRepo()

    abstract Property<String> getImageName()

    abstract ListProperty<String> getTags()

    abstract Property<Boolean> getPull()
}
