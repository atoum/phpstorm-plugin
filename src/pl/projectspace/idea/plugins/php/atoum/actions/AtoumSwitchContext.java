package pl.projectspace.idea.plugins.php.atoum.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.actions.OpenFileAction;

public class AtoumSwitchContext extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(false);
        e.getPresentation().setVisible(true);
        e.getPresentation().setText("atoum - switch to");

        PhpClass testedClass = getSwitchClass(e);
        if (null == testedClass) {
            return;
        }

        e.getPresentation().setText("atoum - switch to : " + testedClass.getFQN());
        e.getPresentation().setEnabled(true);
    }

    public void actionPerformed(final AnActionEvent e) {
        PhpClass testedClass = getSwitchClass(e);
        if (null == testedClass) {
            return;
        }

        OpenFileAction.openFile(testedClass.getContainingFile().getVirtualFile().getPath(), e.getProject());
    }

    @Nullable
    protected PhpClass getSwitchClass(final AnActionEvent e) {
        Object psiFile = e.getData(PlatformDataKeys.PSI_FILE);

        if (null == psiFile) {
            return null;
        }

        if (!(psiFile instanceof PhpFile)) {
            return null;
        }
        PhpFile phpFile = ((PhpFile) psiFile);
        PhpClass testedClass = getSwitchClass(e.getProject(), phpFile);

        if (null == testedClass) {
            return null;
        }

        return testedClass;
    }

    @Nullable
    protected PhpClass getSwitchClass(Project project, PhpFile phpFile) {
        PhpClass currentClass = Utils.getFirstClassFromFile(phpFile);
        if (null == currentClass) {
            return null;
        }

        if (Utils.isClassAtoumTest(currentClass)) {
            return Utils.locateTestedClass(project, currentClass);
        }

        return Utils.locateTestClass(project, currentClass);
    }
}
