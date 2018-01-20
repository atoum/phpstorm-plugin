package org.atoum.intellij.plugin.atoum.run;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.editor.LazyRangeMarkerFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.model.ClassResult;
import org.atoum.intellij.plugin.atoum.model.MethodResult;
import org.atoum.intellij.plugin.atoum.model.TestsResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SMTRootTestProxyFactory {

    public static SMTestProxy.SMRootTestProxy createFromClassResult(ClassResult classResult) {
        SMTestLocator methodLocator = new SMTestLocator() {
            @NotNull
            @Override
            public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope globalSearchScope) {
                String[] parts = path.split(":");

                Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(parts[0]);
                if (classes.isEmpty()) {
                    return new ArrayList<>();
                }

                Method method = classes.iterator().next().findMethodByName(parts[1]);

                if (method == null) {
                    return new ArrayList<>();
                }

                ArrayList<Location> locations = new ArrayList<>(1);
                PsiElement elem = null;

                if (parts.length == 3) {
                    PsiFile file = method.getContainingFile();
                    int line = Integer.parseInt(parts[2]);
                    int offset = LazyRangeMarkerFactory.getInstance(project).createRangeMarker(file.getVirtualFile(), line, 0, false).getStartOffset();
                    elem = file.findElementAt(offset);
                }

                if (elem == null) {
                    elem = method;
                }

                locations.add(new PsiLocation<>(elem));

                return locations;
            }
        };

        SMTestProxy.SMRootTestProxy classNode = new SMTestProxy.SMRootTestProxy();
        classNode.setPresentation(classResult.getName());
        classNode.setFinished();
        classNode.setRootLocationUrl("atoum://" + classResult.getName());

        classNode.setLocator(new SMTestLocator() {
            @NotNull
            @Override
            public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope globalSearchScope) {
                Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(path);
                if (classes.isEmpty()) {
                    return new ArrayList<>();
                }

                ArrayList<Location> locations = new ArrayList<>(1);
                locations.add(new PsiLocation<>(classes.iterator().next()));

                return locations;
            }
        });

        if (classResult.getState().equals(ClassResult.STATE_FAILED)) {
            classNode.setTestFailed("", "", true);
        }

        for (MethodResult methodsResult: classResult.getMethods()) {
            String url = "atoum://" + classResult.getName() + ":" + methodsResult.getName();
            if (methodsResult.getFailLineNumber() != null) {
                url += ":" + methodsResult.getFailLineNumber();
            }

            SMTestProxy methodNode = new SMTestProxy(methodsResult.getName(), false, url);
            methodNode.setLocator(methodLocator);

            if (methodsResult.getState().equals(MethodResult.STATE_FAILED)) {
                methodNode.setTestFailed(methodsResult.getName() + " Failed", methodsResult.getContent(), true);
            } else if (methodsResult.getState().equals(MethodResult.STATE_PASSED)) {
                methodNode.addSystemOutput(methodsResult.getContent());
            }

            methodNode.setFinished();
            classNode.addChild(methodNode);
        }

        return classNode;
    }

    public static void updateFromTestResult(TestsResult testsResult, SMTestProxy testsRootNode) {
        for (ClassResult classResult: testsResult.getClassResults()) {
            testsRootNode.addChild(SMTRootTestProxyFactory.createFromClassResult(classResult));
        }

        if (testsResult.getState().equals(TestsResult.STATE_FAILED)) {
            testsRootNode.setTestFailed("", "", true);
        }
    }

}
