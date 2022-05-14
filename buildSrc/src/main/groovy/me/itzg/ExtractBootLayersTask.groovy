package me.itzg

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

import javax.inject.Inject

abstract class ExtractBootLayersTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getBootJar();

    @OutputDirectory
    abstract DirectoryProperty getLayersDirectory();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    void extract() throws IOException {

        execOperations.javaexec( {
            classpath(getBootJar());
            jvmArgs("-Djarmode=layertools");
            args("extract");
            workingDir(getLayersDirectory());
        })
            .assertNormalExitValue();
    }

}
