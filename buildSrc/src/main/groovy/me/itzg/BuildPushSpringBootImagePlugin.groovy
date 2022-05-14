package me.itzg


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

class BuildPushSpringBootImagePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('plainBootImage', BootImageExtension)
        extension.baseImage.convention("eclipse-temurin:17")
        extension.with {
            imageName.convention(project.name)
            baseImage.convention("eclipse-temurin:17")
            exposePort.convention(8080)
            tags.convention(["latest"])
            pullForBuild.convention(false)
            push.convention(true)
        }

        def dockerClientServiceProvider = project.gradle.sharedServices.registerIfAbsent(
                'dockerClientService', DockerClientService, {}
        )

        final TaskProvider<ExtractBootLayersTask> extractBootLayersTask =
                project.tasks.register("extractBootLayers", ExtractBootLayersTask,
                        {
                            group = "build"
                            layersDirectory.convention(project.layout.buildDirectory.dir("bootImage/layers"))
                            bootJar.set(
                                    project.tasks.named("bootJar", Jar.class)
                                            .flatMap(AbstractArchiveTask::getArchiveFile)
                            )
                        })

        final TaskProvider<GenerateDockerfileTask> generateDockerfileTask =
                project.tasks.register("generatePlainDockerfile", GenerateDockerfileTask,
                        {
                            group = "build"
                            dockerfile.convention(project.layout.buildDirectory.file("bootImage/Dockerfile"))
                        })

        def buildTask = project.tasks.register("buildPlainBootImage", BuildBootImageTask.class,
                {
                    group = "build"
                    dockerfile.set(generateDockerfileTask.flatMap(GenerateDockerfileTask::getDockerfile))
                    layersDirectory.set(extractBootLayersTask.flatMap(ExtractBootLayersTask::getLayersDirectory))
                    bootImageDirectory.convention(project.layout.buildDirectory.dir("bootImage"))
                    bootImageInfoFile.convention(project.layout.buildDirectory.file("boot-image-info.json"))

                    dockerClientService.set(dockerClientServiceProvider)
                    usesService(dockerClientServiceProvider)

                    baseImage.set(extension.baseImage)
                    exposePort.set(extension.exposePort)
                    imageName.set(extension.imageName)
                    pullForBuild.set(extension.pullForBuild)
                    tags.set(extension.tags)
                    imageRepo.set(extension.imageRepo)
                })

        project.tasks.register("pushPlainBootImage", PushBootImageTask, {
            group = "build"
            onlyIf { extension.push.get() }

            dockerClientService.set(dockerClientServiceProvider)
            usesService(dockerClientServiceProvider)

            bootImageInfoFile.set(buildTask.flatMap({it.bootImageInfoFile}))
            imageName.set(extension.imageName)
            tags.set(extension.tags)
            imageRepo.set(extension.imageRepo)
        })
    }

}
