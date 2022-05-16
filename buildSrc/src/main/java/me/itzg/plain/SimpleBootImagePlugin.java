package me.itzg.plain;


import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;

public class SimpleBootImagePlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "simpleBootImage";
    public static final String BUILD_TASK_NAME = "buildSimpleBootImage";
    public static final String PUSH_TASK_NAME = "pushSimpleBootImage";

    protected static final String BOOT_IMAGE_PATH = "simpleBootImage";
    protected static final String LAYERS_SUBPATH = BOOT_IMAGE_PATH + "/layers";
    protected static final String DOCKERFILE_SUBPATH = BOOT_IMAGE_PATH + "/Dockerfile";
    protected static final String FAT_JAR_SUBPATH = BOOT_IMAGE_PATH + "/application.jar";
    protected static final String GROUP = "simple boot image";

    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin("org.springframework.boot")) {
            final BootImageExtension extension = registerExtension(project);

            registerTasks(project, extension);
        }
    }

    private void registerTasks(Project project, BootImageExtension extension) {
        final var extractBootLayersTask =
            project.getTasks().register("extractBootLayers", ExtractBootLayersTask.class,
                task -> {
                    task.setGroup(GROUP);
                    task.onlyIf(spec -> extension.getLayered().get());

                    task.getLayersDirectory().convention(project.getLayout().getBuildDirectory().dir(LAYERS_SUBPATH));
                    task.getBootJar().set(bootJarProvider(project));
                });

        final var stageJarTask = project.getTasks().register("stageBootJarForImage", StageJarTask.class,
            task -> {
                task.setGroup(GROUP);
                task.onlyIf(spec -> !extension.getLayered().get());

                task.getBootJar().set(bootJarProvider(project));
                task.getStagedJar().set(project.getLayout().getBuildDirectory().file(FAT_JAR_SUBPATH));
            }
        );

        final var layeredDockerfileTask =
            project.getTasks().register("generateLayeredDockerfile", GenerateLayeredDockerfileTask.class,
                task -> {
                    task.setGroup(GROUP);
                    task.onlyIf(spec -> extension.getLayered().get());

                    task.getDockerfile().convention(project.getLayout().getBuildDirectory().file(DOCKERFILE_SUBPATH));
                });

        final var fatJarDockerfileTask =
            project.getTasks().register("generateFatJarDockerfile", GenerateFatJarDockerfileTask.class,
                task -> {
                    task.setGroup(GROUP);
                    task.onlyIf(spec -> !extension.getLayered().get());

                    task.getDockerfile().convention(project.getLayout().getBuildDirectory().file(DOCKERFILE_SUBPATH));

                    task.getStagedJar().set(stageJarTask.flatMap(StageJarTask::getStagedJar));
                }
            );

        final var buildTask = project.getTasks().register(BUILD_TASK_NAME, BuildImageTask.class,
            task -> {
                task.setGroup(GROUP);
                if (extension.getLayered().get()) {
                    task.getDockerfile().set(layeredDockerfileTask.flatMap(GenerateLayeredDockerfileTask::getDockerfile));
                    task.dependsOn(extractBootLayersTask);
                } else {
                    task.getDockerfile().set(fatJarDockerfileTask.flatMap(GenerateFatJarDockerfileTask::getDockerfile));
                    task.dependsOn(stageJarTask);
                }
                task.getBootImageDirectory().convention(project.getLayout().getBuildDirectory().dir(BOOT_IMAGE_PATH));

                task.apply(extension);
            });

        project.getTasks().register(PUSH_TASK_NAME, PushImageTask.class,
            task -> {
                task.onlyIf(spec -> extension.getPush().get());
                task.setGroup(GROUP);
                task.dependsOn(buildTask);

                task.apply(extension);
            });
    }

    private BootImageExtension registerExtension(Project project) {
        var extension = project.getExtensions().create(EXTENSION_NAME, BootImageExtension.class);
        extension.getBaseImage().convention("eclipse-temurin:17");
        extension.getImageName().convention(project.getName());
        extension.getExposePort().convention(8080);
        extension.getTags().convention(List.of("latest"));
        extension.getPullForBuild().convention(false);
        extension.getPush().convention(true);
        extension.getLayered().convention(true);
        return extension;
    }

    private Provider<RegularFile> bootJarProvider(Project project) {
        return project.getTasks().named("bootJar", Jar.class)
            .flatMap(AbstractArchiveTask::getArchiveFile);
    }

}
