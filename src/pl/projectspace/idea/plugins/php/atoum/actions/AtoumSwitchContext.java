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
        e.getPresentation().setText("Go to atoum test");

        PhpClass testedClass = getTestedClass(e);
        if (null == testedClass) {
            return;
        }

        e.getPresentation().setText("Go to atoum test : " + testedClass.getFQN());
        e.getPresentation().setEnabled(true);
    }

    public void actionPerformed(final AnActionEvent e) {
        PhpClass testedClass = getTestedClass(e);
        if (null == testedClass) {
            return;
        }
        OpenFileAction.openFile(testedClass.getContainingFile().getVirtualFile().getPath(), e.getProject());
    }

    @Nullable
    protected PhpClass getTestedClass(final AnActionEvent e) {
        Object psiFile = e.getData(PlatformDataKeys.PSI_FILE);

        if (null == psiFile) {
            return null;
        }

        if (psiFile instanceof PhpFile) {
            PhpFile phpFile = ((PhpFile) psiFile);
            PhpClass testedClass = getTestedClass(e.getProject(), phpFile);
            if (null == testedClass) {
                return null;
            }

            return testedClass;
        }

        return null;
    }

    @Nullable
    protected PhpClass getTestedClass(Project project, PhpFile phpFile) {
        PhpClass currentClass = getFirstClassFromFile(phpFile);
        if (null == currentClass) {
            return null;
        }

        String testedClassname = currentClass.getNamespaceName() + "tests\\units\\" + currentClass.getName();
        Collection<PhpClass> phpClasses = PhpIndex.getInstance(project).getAnyByFQN(testedClassname);
        if (phpClasses.size() == 1) {
            PhpClass testedClass = (PhpClass)phpClasses.toArray()[0];
            return testedClass;
        }

        return null;
    }

    //https://github.com/Haehnchen/idea-php-symfony2-plugin/blob/cb422db9779025d65fdf0ba5d26a38d401eca939/src/fr/adrienbrault/idea/symfony2plugin/util/PhpElementsUtil.java#L784
    @Nullable
    private PhpClass getFirstClassFromFile(PhpFile phpFile) {
        Collection<PhpClass> phpClasses = PsiTreeUtil.collectElementsOfType(phpFile, PhpClass.class);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }

}
