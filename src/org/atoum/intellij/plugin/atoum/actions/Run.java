package org.atoum.intellij.plugin.atoum.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
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

    protected void saveFiles(PhpClass currentTestClass, Project project) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

        Document documentTestClass = fileDocumentManager.getDocument(currentTestClass.getContainingFile().getVirtualFile());
        if (documentTestClass != null) {
            fileDocumentManager.saveDocument(documentTestClass);
        }

        PhpClass currentTestedClass = Utils.locateTestedClass(project, currentTestClass);
        if (currentTestedClass != null) {
            Document documentTestedClass = fileDocumentManager.getDocument(currentTestedClass.getContainingFile().getVirtualFile());
            if (documentTestedClass != null) {
                fileDocumentManager.saveDocument(documentTestedClass);
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
            saveFiles(currentTestClass, project);
            runConfiguration.setFile((PhpFile)currentTestClass.getContainingFile());

            Method currentTestMethod = getCurrentTestMethod(e);
            if (currentTestMethod != null) {
                runConfiguration.setMethod(currentTestMethod);
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
        VirtualFile virtualFile = getVirtualFile(e);

        if (null == virtualFile) {
            return null;
        }

        if (!virtualFile.isDirectory()) {
            return null;
        }

        return virtualFile;
    }

    @Nullable
    protected PhpClass getCurrentTestClass(AnActionEvent e) {
        PhpFile phpFile = getPhpFile(e);

        if (null == phpFile) {
            return null;
        }

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
        PhpFile file = getPhpFile(e);
        Editor editor = getEditor(e);

        if (file == null || editor == null) {
            return null;
        }

        Method method = PsiTreeUtil.findElementOfClassAtOffset(file, editor.getCaretModel().getOffset(), Method.class, false);

        if (method != null && method.getName().startsWith("test")) {
            return method;
        }

        return null;
    }

    @Nullable
    private PhpFile getPhpFile(AnActionEvent e)
    {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);

        if (file instanceof PhpFile) {
            return (PhpFile) file;
        }

        return null;
    }

    @Nullable
    private Editor getEditor(AnActionEvent e)
    {
        return CommonDataKeys.EDITOR.getData(e.getDataContext());
    }

    @Nullable
    private VirtualFile getVirtualFile(AnActionEvent e)
    {
        return CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
    }
}
