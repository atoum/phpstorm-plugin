package org.atoum.intellij.plugin.atoum;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class Utils {

    public static Boolean isClassAtoumTest(PhpClass checkedClass)
    {
        // First, we check if the class is in the units tests namespace
        if (!checkedClass.getNamespaceName().toLowerCase().contains(getTestsNamespaceSuffix(checkedClass).toLowerCase())) {
            return false;
        }

        if (checkedClass.isAbstract() || checkedClass.isInterface()) {
            return false;
        }

        // We also check if the class extends atoum
        while (checkedClass.getSuperClass() != null) {
            PhpClass parent = checkedClass.getSuperClass();
            if (parent.getFQN().equals("\\atoum")) {
                return true;
            }
            checkedClass = parent;
        }

        return false;
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
        // Search classes with same name
        for (PhpClass similarClass : PhpIndex.getInstance(project).getClassesByName(testedClass.getName())) {
            // Skip testedClass
            if (similarClass.getFQN().equals(testedClass.getFQN())) {
                continue;
            }

            // Skip classes which don't have the same base namespace
            if (!similarClass.getNamespaceName().startsWith(testedClass.getNamespaceName())) {
                continue;
            }

            // Skip non atoum classes
            if (!isClassAtoumTest(similarClass)) {
                continue;
            }

            ArrayList<PhpClass> data = new ArrayList<>();
            data.add(similarClass);

            return data;
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
        String testedClassname = testClassNamespaceName.toLowerCase().replace(getTestsNamespaceSuffix(testClass).toLowerCase(), "") + testClass.getName();

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

    private static String getTestsNamespaceSuffix(PhpClass phpClass)
    {
        while (phpClass != null) {
            // Check @namespace annotation in the class
            PhpDocComment docComment = phpClass.getDocComment();
            if (docComment != null) {
                PhpDocTag[] tags = docComment.getTagElementsByName("@namespace");

                if (tags.length > 0) {
                    return checkBackslashes(tags[0].getTagValue());
                }
            }

            // Check setTestNamespace call in __construct
            Method constructor = phpClass.getOwnConstructor();
            if (constructor != null) {
                MethodReference setTestNamespaceCall = findMethodCallInElement(constructor, "setTestNamespace");

                if (setTestNamespaceCall != null && setTestNamespaceCall.getParameters().length > 0) {
                    PsiElement callParameter = setTestNamespaceCall.getParameters()[0];

                    if (callParameter instanceof StringLiteralExpression) {
                        return checkBackslashes(((StringLiteralExpression) callParameter).getContents());
                    }
                }
            }

            // Not found? Try with parent class
            phpClass = phpClass.getSuperClass();
        }

        // Default value if no @namespace or $this->setTestNamespace() were found
        return "tests\\units\\";
    }

    /**
     * Ensure namespace doesn't start with \ but ends with \
     */
    private static String checkBackslashes(String namespace)
    {
        if (namespace.startsWith("\\")) {
            namespace = namespace.substring(1);
        }

        if (!namespace.endsWith("\\")) {
            namespace += "\\";
        }

        return namespace;
    }

    @Nullable
    private static MethodReference findMethodCallInElement(PsiElement element, String name)
    {
        for (PsiElement child : element.getChildren())
        {
            if (child instanceof MethodReference) {
                MethodReference method = (MethodReference) child;

                if (method.getName() != null && method.getName().equals(name)) {
                    return method;
                }
            }

            MethodReference result = findMethodCallInElement(child, name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    //https://github.com/Haehnchen/idea-php-symfony2-plugin/blob/cb422db9779025d65fdf0ba5d26a38d401eca939/src/fr/adrienbrault/idea/symfony2plugin/util/PhpElementsUtil.java#L784
    @Nullable
    public static PhpClass getFirstClassFromFile(PhpFile phpFile) {
        Collection<PhpClass> phpClasses = PsiTreeUtil.collectElementsOfType(phpFile, PhpClass.class);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }

    @Nullable
    public static PhpClass getFirstTestClassFromFile(PhpFile phpFile) {
        Collection<PhpClass> phpClasses = PsiTreeUtil.collectElementsOfType(phpFile, PhpClass.class);

        for (PhpClass phpClass:phpClasses) {
            if (isClassAtoumTest(phpClass)) {
                return phpClass;
            }
        }

        return null;
    }
}
