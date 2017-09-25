package org.atoum.intellij.plugin.atoum.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;
import org.atoum.intellij.plugin.atoum.run.Runner;

public class RunAll extends AnAction {

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(true);
        event.getPresentation().setVisible(true);
        event.getPresentation().setText("atoum - run all tests");
    }

    public void actionPerformed(final AnActionEvent e) {
        Project project = e.getProject();
        RunnerConfiguration runConfiguration = new RunnerConfiguration();
        Runner runner = new Runner(project);
        runner.run(runConfiguration);
    }
}
