package pl.projectspace.idea.plugins.php.atoum.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.actions.OpenFileAction;
import java.util.Collection;

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
        PhpClass currentClass = getFirstClassFromFile(phpFile);
        if (null == currentClass) {
            return null;
        }

        if (isClassAtoumTest(currentClass)) {
            return locateTestedClass(project, currentClass);
        }

        return locateTestClass(project, currentClass);
    }

    protected Boolean isClassAtoumTest(PhpClass checkedClass)
    {
        return checkedClass.getNamespaceName().endsWith(getTestsNamespaceSuffix());
    }

    @Nullable
    protected PhpClass locateTestClass(Project project, PhpClass testedClass) {
        String testClassname = testedClass.getNamespaceName() + getTestsNamespaceSuffix() + testedClass.getName();
        return locatePhpClass(project, testClassname);
    }

    @Nullable
    protected PhpClass locateTestedClass(Project project, PhpClass testClass) {
        String testClassNamespaceName = testClass.getNamespaceName();
        String testedClassname = testClassNamespaceName.substring(0, testClassNamespaceName.length() - getTestsNamespaceSuffix().length()) + testClass.getName();
        return locatePhpClass(project, testedClassname);

    }

    @Nullable
    protected PhpClass locatePhpClass(Project project, String name) {
        Collection<PhpClass> phpClasses = PhpIndex.getInstance(project).getAnyByFQN(name);
        if (phpClasses.size() != 1) {
            return null;
        }

        return (PhpClass)phpClasses.toArray()[0];
    }

    private String getTestsNamespaceSuffix()
    {
        return "tests\\units\\";
    }

    //https://github.com/Haehnchen/idea-php-symfony2-plugin/blob/cb422db9779025d65fdf0ba5d26a38d401eca939/src/fr/adrienbrault/idea/symfony2plugin/util/PhpElementsUtil.java#L784
    @Nullable
    private PhpClass getFirstClassFromFile(PhpFile phpFile) {
        Collection<PhpClass> phpClasses = PsiTreeUtil.collectElementsOfType(phpFile, PhpClass.class);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }

}
