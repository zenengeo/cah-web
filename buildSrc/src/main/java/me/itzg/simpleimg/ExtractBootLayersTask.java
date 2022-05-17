package me.itzg.simpleimg;

import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

public abstract class ExtractBootLayersTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getBootJar();

    @OutputDirectory
    abstract DirectoryProperty getLayersDirectory();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    void extract() {

        getExecOperations()
            .javaexec(spec -> {
                spec.classpath(getBootJar());
                spec.jvmArgs("-Djarmode=layertools");
                spec.args("extract");
                spec.workingDir(getLayersDirectory());
            })
            .assertNormalExitValue();
    }

}
