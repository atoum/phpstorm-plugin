package org.atoum.intellij.plugin.atoum.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.Utils;
import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;
import org.atoum.intellij.plugin.atoum.run.Runner;
import org.jetbrains.annotations.Nullable;

public class RunAll extends AnAction {

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(true);
        event.getPresentation().setVisible(true);
        event.getPresentation().setText("atoum - run all tests");
    }

    protected void saveFiles(PhpClass currentTestClass, Project project) {
        Document documentTestClass = FileDocumentManager.getInstance().getDocument(currentTestClass.getContainingFile().getVirtualFile());
        Document documentTestedClass = FileDocumentManager.getInstance().getDocument(Utils.locateTestedClass(project, currentTestClass).getContainingFile().getVirtualFile());
        FileDocumentManager.getInstance().saveDocument(documentTestClass);
        FileDocumentManager.getInstance().saveDocument(documentTestedClass);

    }

    public void actionPerformed(final AnActionEvent e) {
        Project project = e.getProject();
        RunnerConfiguration runConfiguration = new RunnerConfiguration();
        Runner runner = new Runner(project);
        runner.run(runConfiguration);
    }
}
