package me.itzg.plain;


import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;

public class BuildPushSpringBootImagePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("plainBootImage", BootImageExtension.class);
        extension.getBaseImage().convention("eclipse-temurin:17");
        extension.getImageName().convention(project.getName());
        extension.getExposePort().convention(8080);
        extension.getTags().convention(List.of("latest"));
        extension.getPullForBuild().convention(false);
        extension.getPush().convention(true);

        var dockerClientServiceProvider = project.getGradle().getSharedServices().registerIfAbsent(
            "dockerClientService", DockerClientService.class, spec -> {
            }
        );

        final var extractBootLayersTask =
            project.getTasks().register("extractBootLayers", ExtractBootLayersTask.class,
                task -> {
                    task.setGroup("build");
                    task.getLayersDirectory().convention(project.getLayout().getBuildDirectory().dir("bootImage/layers"));
                    task.getBootJar().set(
                        project.getTasks().named("bootJar", Jar.class)
                            .flatMap(AbstractArchiveTask::getArchiveFile)
                    );
                });

        final var generateDockerfileTask =
            project.getTasks().register("generatePlainDockerfile", GenerateDockerfileTask.class,
                task -> {
                    task.getDockerfile().convention(project.getLayout().getBuildDirectory().file("bootImage/Dockerfile"));
                });

        final var buildTask = project.getTasks().register("buildPlainBootImage", BuildImageTask.class,
            task -> {
                task.setGroup("build");
                task.getDockerfile().set(generateDockerfileTask.flatMap(GenerateDockerfileTask::getDockerfile));
                task.getLayersDirectory().set(extractBootLayersTask.flatMap(ExtractBootLayersTask::getLayersDirectory));
                task.getBootImageDirectory().convention(project.getLayout().getBuildDirectory().dir("bootImage"));
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
                task.setGroup("build");
                task.onlyIf(spec -> extension.getPush().get());

                task.getDockerClientService().set(dockerClientServiceProvider);
                task.usesService(dockerClientServiceProvider);

                task.getBootImageInfoFile().set(buildTask.flatMap(BuildImageTask::getBootImageInfoFile));
                task.getImageName().set(extension.getImageName());
                task.getTags().set(extension.getTags());
                task.getImageRepo().set(extension.getImageRepo());
            });
    }

}
