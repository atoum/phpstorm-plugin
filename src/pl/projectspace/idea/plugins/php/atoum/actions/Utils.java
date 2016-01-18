package pl.projectspace.idea.plugins.php.atoum.actions;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class Utils {

    public static Boolean isClassAtoumTest(PhpClass checkedClass)
    {
        return checkedClass.getNamespaceName().contains(getTestsNamespaceSuffix());
    }

    @Nullable
    public static PhpClass locateTestClass(Project project, PhpClass testedClass) {
        String[] namespaceParts = testedClass.getNamespaceName().split("\\\\");

        for(int i=namespaceParts.length; i>=1; i--){
            List<String> foo = Lists.newArrayList(namespaceParts);
            foo.add(i, getTestsNamespaceSuffix().substring(0, getTestsNamespaceSuffix().length() - 1));

            String possibleClassname = StringUtils.join(foo, "\\") + "\\" + testedClass.getName();
            System.out.println(possibleClassname);
            PhpClass possibleClass = locatePhpClass(project, possibleClassname);
            if (null != possibleClass) {
                return possibleClass;
            }
        }

        return null;
    }

    @Nullable
    public static PhpClass locateTestedClass(Project project, PhpClass testClass) {
        String testClassNamespaceName = testClass.getNamespaceName();
        String testedClassname = testClassNamespaceName.replace(getTestsNamespaceSuffix(), "") + testClass.getName();
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
