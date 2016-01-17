package pl.projectspace.idea.plugins.php.atoum.actions;

import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class Utils {

    public static Boolean isClassAtoumTest(PhpClass checkedClass)
    {
        return checkedClass.getNamespaceName().endsWith(getTestsNamespaceSuffix());
    }

    @Nullable
    public static PhpClass locateTestClass(Project project, PhpClass testedClass) {
        String testClassname = testedClass.getNamespaceName() + getTestsNamespaceSuffix() + testedClass.getName();
        return locatePhpClass(project, testClassname);
    }

    @Nullable
    public static PhpClass locateTestedClass(Project project, PhpClass testClass) {
        String testClassNamespaceName = testClass.getNamespaceName();
        String testedClassname = testClassNamespaceName.substring(0, testClassNamespaceName.length() - getTestsNamespaceSuffix().length()) + testClass.getName();
        return locatePhpClass(project, testedClassname);

    }

    @Nullable
    protected static PhpClass locatePhpClass(Project project, String name) {
        Collection<PhpClass> phpClasses = PhpIndex.getInstance(project).getAnyByFQN(name);
        if (phpClasses.size() != 1) {
            return null;
        }

        return (PhpClass)phpClasses.toArray()[0];
    }

    private static String getTestsNamespaceSuffix()
    {
        return "tests\\units\\";
    }

    //https://github.com/Haehnchen/idea-php-symfony2-plugin/blob/cb422db9779025d65fdf0ba5d26a38d401eca939/src/fr/adrienbrault/idea/symfony2plugin/util/PhpElementsUtil.java#L784
    @Nullable
    public static PhpClass getFirstClassFromFile(PhpFile phpFile) {
        Collection<PhpClass> phpClasses = PsiTreeUtil.collectElementsOfType(phpFile, PhpClass.class);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }
}