package org.atoum.intellij.plugin.atoum;

import com.google.common.collect.Lists;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {

    public static Boolean isClassAtoumTest(PhpClass checkedClass)
    {
        return checkedClass.getNamespaceName().toLowerCase().contains(getTestsNamespaceSuffix().toLowerCase());
    }

    @Nullable
    public static PhpClass locateTestClass(Project project, PhpClass testedClass) {
        Collection<PhpClass> possibleClasses = locateTestClasses(project, testedClass);
        if (possibleClasses.size() > 0) {
            return (PhpClass)possibleClasses.toArray()[0];
        }

        return null;
    }

    @Nullable
    public static Collection<PhpClass> locateTestClasses(Project project, PhpClass testedClass) {
        if (testedClass.getNamespaceName().length() == 1) {
            Collection<PhpClass> foundClasses = locatePhpClasses(project, getTestsNamespaceSuffix() + testedClass.getName());
            if (foundClasses.size() > 0) {
                return foundClasses;
            }
        }

        String[] namespaceParts = testedClass.getNamespaceName().split("\\\\");

        for(int i=namespaceParts.length; i>=1; i--){
            List<String> foo = Lists.newArrayList(namespaceParts);
            foo.add(i, getTestsNamespaceSuffix().substring(0, getTestsNamespaceSuffix().length() - 1));

            String possibleClassname = StringUtils.join(foo, "\\") + "\\" + testedClass.getName();
            Collection<PhpClass> foundClasses = locatePhpClasses(project, possibleClassname);
            if (foundClasses.size() > 0) {
                return foundClasses;
            }
        }

        return new ArrayList<PhpClass>();
    }

    @Nullable
    public static PhpClass locateTestedClass(Project project, PhpClass testClass) {
        Collection<PhpClass> possibleClasses = locateTestedClasses(project, testClass);
        if (possibleClasses.size() > 0) {
            return (PhpClass)possibleClasses.toArray()[0];
        }

        return null;
    }

    @Nullable
    public static Collection<PhpClass> locateTestedClasses(Project project, PhpClass testClass) {
        String testClassNamespaceName = testClass.getNamespaceName();
        String testedClassname = testClassNamespaceName.toLowerCase().replace(getTestsNamespaceSuffix().toLowerCase(), "") + testClass.getName();
        return locatePhpClasses(project, testedClassname);
    }

    @Nullable
    protected static PhpClass locatePhpClass(Project project, String name) {
        Collection<PhpClass> phpClasses = locatePhpClasses(project, name);
        if (phpClasses.size() != 1) {
            return null;
        }

        return (PhpClass)phpClasses.toArray()[0];
    }

    @Nullable
    protected static Collection<PhpClass> locatePhpClasses(Project project, String name) {
        return PhpIndex.getInstance(project).getAnyByFQN(name);
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

    public static void saveFiles(PhpClass currentTestClass, Project project) {
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
}
