package me.itzg.plain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

public abstract class BuildImageTask extends ImageHandlingTask {

    @InputDirectory
    abstract DirectoryProperty getBootImageDirectory();

    @InputFile
    abstract RegularFileProperty getDockerfile();

    @Input
    abstract Property<String> getBaseImage();

    @Input
    abstract Property<Integer> getExposePort();

    @Input
    abstract Property<Boolean> getUseBuildx();

    @Input
    abstract Property<Boolean> getPullForBuild();

    @Optional
    @Input
    abstract Property<String> getCacheFrom();

    @Optional
    @Input
    abstract Property<String> getCacheTo();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Override
    void apply(BootImageExtension extension) {
        getBaseImage().set(extension.getBaseImage());
        getExposePort().set(extension.getExposePort());
        getUseBuildx().set(extension.getUseBuildx());
        getPullForBuild().set(extension.getPullForBuild());
        getCacheFrom().set(extension.getCacheFrom());
        getCacheTo().set(extension.getCacheTo());

        super.apply(extension);
    }

    @TaskAction
    void build() throws IOException {
        final var fullImageName = calculateFullImageName();

        getLogger().info("Building {} with base image {} tagged with {}",
            fullImageName, getBaseImage().get(), getTags().get());

        if (getLogger().isTraceEnabled()) {
            try (Stream<Path> pathStream = Files.walk(getBootImageDirectory().get().getAsFile().toPath())) {
                pathStream
                    .forEach(path -> getLogger().trace("Context: {}{}", path, Files.isDirectory(path) ? "/" : ""));
            }
        }

        getExecOperations()
            .exec(spec -> {
                spec.executable("docker");
                spec.args(createArgsList());

                getLogger().debug("Executing: docker {}", spec.getArgs());
            })
            .assertNormalExitValue();
    }

    private List<String> createArgsList() {
        final ArrayList<String> args = new ArrayList<>();
        if (needsBuildx()) {
            args.add("buildx");
        }
        args.add("build");

        addBuildArg(args, "BASE_IMG", getBaseImage().get());
        addBuildArg(args, "EXPOSE_PORT", getExposePort().get());

        final var imageTags = expandImageTags();
        for (final String imageTag : imageTags) {
            args.add("--tag");
            args.add(imageTag);
        }

        args.add("--file");
        args.add(getDockerfile().get().getAsFile().getPath());

        addOptionalArg(args, "--cache-from", getCacheFrom());
        addOptionalArg(args, "--cache-to", getCacheTo());

        if (getPullForBuild().get()) {
            args.add("--pull");
        }

        args.add(getBootImageDirectory().get().getAsFile().getPath());

        return args;
    }

    private void addOptionalArg(ArrayList<String> args, String arg, Property<String> value) {
        if (value.isPresent()) {
            args.add(arg);
            args.add(value.get());
        }
    }

    private boolean needsBuildx() {
        return getUseBuildx().get() || getCacheTo().isPresent();
    }

    private void addBuildArg(ArrayList<String> args, String name, Object value) {
        args.add("--build-arg");
        args.add(name + "=" + value);
    }

}
