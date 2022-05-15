package me.itzg.plain;

import org.gradle.api.provider.Property;

public class FullImageName {

    private FullImageName() {
    }

    static String calculate(Property<String> imageRepo, Property<String> imageName) {
        return imageRepo.isPresent() ? imageRepo.get() + "/" + imageName.get() : imageName.get();
    }
}
