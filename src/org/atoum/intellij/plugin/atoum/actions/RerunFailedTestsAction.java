package org.atoum.intellij.plugin.atoum.actions;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.Utils;
import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;
import org.atoum.intellij.plugin.atoum.run.Runner;

public class RerunFailedTestsAction extends AnAction
{
    private SMTestProxy.SMRootTestProxy tests;

    // Action class must have a no argument constructor
    public RerunFailedTestsAction() {
        this.getTemplatePresentation().setText("Rerun Failed Tests");
        this.getTemplatePresentation().setIcon(AllIcons.RunConfigurations.RerunFailedTests);
    }

    public RerunFailedTestsAction(SMTestProxy.SMRootTestProxy tests)
    {
        this();
        this.tests = tests;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(false);
        presentation.setVisible(true);

        if (!this.tests.isInProgress() && !this.tests.isPassed()) {
            presentation.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent)
    {
        RunnerConfiguration runConfiguration = new RunnerConfiguration();
        Project project = getEventProject(anActionEvent);

        for (SMTestProxy methodProxy: tests.getAllTests()) {
            if (!methodProxy.isPassed()) {
                Location loc = methodProxy.getLocation(project, GlobalSearchScope.EMPTY_SCOPE);

                if (loc == null) {
                    continue;
                }

                PsiElement elem = loc.getPsiElement();

                if (elem instanceof PhpClass) {
                    Utils.saveFiles((PhpClass) elem, project);
                    runConfiguration.setFile((PhpFile)elem.getContainingFile());
                    continue;
                }

                while (elem != null && !(elem instanceof Method)) {
                    elem = elem.getParent();
                }

                if (elem != null) {
                    runConfiguration.addMethod((Method) elem);
                }
            }
        }

        Runner runner = new Runner(project);
        runner.run(runConfiguration);
    }
}
