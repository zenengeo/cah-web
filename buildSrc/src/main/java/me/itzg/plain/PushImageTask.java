package me.itzg.plain;

import javax.inject.Inject;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

public abstract class PushImageTask extends ImageHandlingTask {

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    void push() throws InterruptedException {
        var fullImageName = calculateFullImageName();

        for (final String tag : getTags().get()) {
            getLogger().info("Pushing image {}:{}", fullImageName, tag);

            getExecOperations().exec(spec -> {
                spec.executable("docker");
                spec.args("push", fullImageName+":"+tag);
            }).assertNormalExitValue();


        }
    }

}
