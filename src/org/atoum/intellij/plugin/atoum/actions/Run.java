package org.atoum.intellij.plugin.atoum.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;
import org.atoum.intellij.plugin.atoum.run.Runner;
import org.jetbrains.annotations.Nullable;
import org.atoum.intellij.plugin.atoum.Utils;

public class Run extends AnAction {

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(false);
        event.getPresentation().setVisible(true);
        event.getPresentation().setText("atoum - run test");

        PhpClass currentTestClass = getCurrentTestClass(event);
        if (currentTestClass != null) {
            event.getPresentation().setEnabled(true);
            Method currentTestMethod = getCurrentTestMethod(event);
            if (currentTestMethod != null) {
                event.getPresentation().setText("atoum - run " + currentTestClass.getName() + "::" + currentTestMethod.getName());
            } else {
                event.getPresentation().setText("atoum - run test : " + currentTestClass.getName());
            }
        } else {
            VirtualFile selectedDir = getCurrentTestDirectory(event);
            if (selectedDir != null) {
                event.getPresentation().setText("atoum - run dir : " + selectedDir.getName());
                event.getPresentation().setEnabled(true);
            }
        }
    }

    public void actionPerformed(final AnActionEvent e) {
        PhpClass currentTestClass = getCurrentTestClass(e);
        VirtualFile selectedDir = null;
        if (currentTestClass == null) {
            selectedDir = getCurrentTestDirectory(e);
            if (null == selectedDir) {
                return;
            }
        }

        Project project = e.getProject();

        RunnerConfiguration runConfiguration = new RunnerConfiguration();
        if (null != currentTestClass) {
            Utils.saveFiles(currentTestClass, project);
            runConfiguration.setFile((PhpFile)currentTestClass.getContainingFile());

            Method currentTestMethod = getCurrentTestMethod(e);
            if (currentTestMethod != null) {
                runConfiguration.addMethod(currentTestMethod);
            }
        }

        if (null != selectedDir) {
            runConfiguration.setDirectory(selectedDir);
        }

        Runner runner = new Runner(project);
        runner.run(runConfiguration);
    }


    @Nullable
    protected VirtualFile getCurrentTestDirectory(AnActionEvent e)
    {
        Object eventVirtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        if (null == eventVirtualFile) {
            return null;
        }

        if (!(eventVirtualFile instanceof VirtualFile)) {
            return null;
        }

        VirtualFile virtualFile = ((VirtualFile) eventVirtualFile);

        if (!virtualFile.isDirectory()) {
            return null;
        }

        return virtualFile;
    }

    @Nullable
    protected PhpClass getCurrentTestClass(AnActionEvent e) {
        Object psiFile = e.getData(PlatformDataKeys.PSI_FILE);

        if (null == psiFile) {
            return null;
        }

        if (!(psiFile instanceof PhpFile)) {
            return null;
        }
        PhpFile phpFile = ((PhpFile) psiFile);

        PhpClass currentClass = Utils.getFirstClassFromFile(phpFile);
        if (null == currentClass) {
            return null;
        }

        if (!Utils.isClassAtoumTest(currentClass)) {
            return Utils.locateTestClass(e.getProject(), currentClass);
        }

        return currentClass;
    }

    @Nullable
    protected Method getCurrentTestMethod(AnActionEvent e) {
        PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);

        if (psiElement != null && psiElement instanceof Method) {
            Method method = (Method) psiElement;
            if (method.getName().startsWith("test")) {
                return method;
            }
        }

        return null;
    }
}
