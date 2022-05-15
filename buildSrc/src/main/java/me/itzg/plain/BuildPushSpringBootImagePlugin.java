package me.itzg.plain;


import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;

public class BuildPushSpringBootImagePlugin implements Plugin<Project> {

    protected static final String BOOT_IMAGE_PATH = "bootImage";
    protected static final String LAYERS_SUBPATH = "bootImage/layers";
    protected static final String DOCKERFILE_SUBPATH = "bootImage/Dockerfile";
    protected static final String FAT_JAR_SUBPATH = "bootImage/application.jar";

    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin("org.springframework.boot")) {
            final BootImageExtension extension = registerExtension(project);

            final Provider<DockerClientService> dockerClientServiceProvider = registerDockerClientService(project);

            registerTasks(project, extension, dockerClientServiceProvider);
        }
    }

    private void registerTasks(Project project, BootImageExtension extension,
        Provider<DockerClientService> dockerClientServiceProvider) {
        final var extractBootLayersTask =
            project.getTasks().register("extractBootLayers", ExtractBootLayersTask.class,
                task -> {
                    task.setGroup("build");
                    task.onlyIf(spec -> extension.getLayered().get());

                    task.getLayersDirectory().convention(project.getLayout().getBuildDirectory().dir(LAYERS_SUBPATH));
                    task.getBootJar().set(bootJarProvider(project));
                });

        final var stageJarTask = project.getTasks().register("stageBootJarForImage", StageJarTask.class,
            task -> {
                task.setGroup("build");
                task.onlyIf(spec -> !extension.getLayered().get());

                task.getBootJar().set(bootJarProvider(project));
                task.getStagedJar().set(project.getLayout().getBuildDirectory().file(FAT_JAR_SUBPATH));
            }
        );

        final var layeredDockerfileTask =
            project.getTasks().register("generateLayeredDockerfile", GenerateLayeredDockerfileTask.class,
                task -> {
                    task.setGroup("build");
                    task.onlyIf(spec -> extension.getLayered().get());

                    task.getDockerfile().convention(project.getLayout().getBuildDirectory().file(DOCKERFILE_SUBPATH));
                });

        final var fatJarDockerfileTask =
            project.getTasks().register("generateFatJarDockerfile", GenerateFatJarDockerfileTask.class,
                task -> {
                    task.setGroup("build");
                    task.onlyIf(spec -> !extension.getLayered().get());

                    task.getDockerfile().convention(project.getLayout().getBuildDirectory().file(DOCKERFILE_SUBPATH));

                    task.getStagedJar().set(stageJarTask.flatMap(StageJarTask::getStagedJar));
                }
            );

        final var buildTask = project.getTasks().register("buildPlainBootImage", BuildImageTask.class,
            task -> {
                task.setGroup("build");
                if (extension.getLayered().get()) {
                    task.getDockerfile().set(layeredDockerfileTask.flatMap(GenerateLayeredDockerfileTask::getDockerfile));
                    task.dependsOn(extractBootLayersTask);
                } else {
                    task.getDockerfile().set(fatJarDockerfileTask.flatMap(GenerateFatJarDockerfileTask::getDockerfile));
                    task.dependsOn(stageJarTask);
                }
                task.getBootImageDirectory().convention(project.getLayout().getBuildDirectory().dir(BOOT_IMAGE_PATH));
                task.getBootImageInfoFile().convention(project.getLayout().getBuildDirectory().file("boot-image-info.json"));

                task.getDockerClientService().set(dockerClientServiceProvider);
                task.usesService(dockerClientServiceProvider);

                task.getBaseImage().set(extension.getBaseImage());
                task.getExposePort().set(extension.getExposePort());
                task.getImageName().set(extension.getImageName());
                task.getPullForBuild().set(extension.getPullForBuild());
                task.getTags().set(extension.getTags());
                task.getImageRepo().set(extension.getImageRepo());
            });

        project.getTasks().register("pushPlainBootImage", PushImageTask.class,
            task -> {
                task.onlyIf(spec -> extension.getPush().get());
                task.setGroup("build");
                task.dependsOn(buildTask);

                task.getDockerClientService().set(dockerClientServiceProvider);
                task.usesService(dockerClientServiceProvider);

                task.getImageName().set(extension.getImageName());
                task.getTags().set(extension.getTags());
                task.getImageRepo().set(extension.getImageRepo());
            });
    }

    private Provider<DockerClientService> registerDockerClientService(Project project) {
        return project.getGradle().getSharedServices().registerIfAbsent(
            "dockerClientService", DockerClientService.class, spec -> {
            }
        );
    }

    private BootImageExtension registerExtension(Project project) {
        var extension = project.getExtensions().create("plainBootImage", BootImageExtension.class);
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
