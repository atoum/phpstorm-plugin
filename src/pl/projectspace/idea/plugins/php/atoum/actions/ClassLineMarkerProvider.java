package pl.projectspace.idea.plugins.php.atoum.actions;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ClassLineMarkerProvider implements com.intellij.codeInsight.daemon.LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> results) {
        if(psiElements.size() == 0) {
            return;
        }

        Project project = psiElements.get(0).getProject();

        for(PsiElement psiElement: psiElements) {
            if(psiElement instanceof PhpClass) {
                classNameMarker((PhpClass)psiElement, results, project);
            }
        }
    }

    private void classNameMarker(PhpClass currentClass, Collection<? super RelatedItemLineMarkerInfo> result, Project project) {
        PhpClass target;
        String tooltip;

        if (Utils.isClassAtoumTest(currentClass)) {
            target = Utils.locateTestedClass(project, currentClass);
            tooltip = "Navigate to tested class";
        } else {
            target = Utils.locateTestClass(project, currentClass);
            tooltip = "Navigate to test";
        }

        if (target == null) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(Icons.ATOUM).
                setTarget(target).
                setTooltipText(tooltip);
        result.add(builder.createLineMarkerInfo(currentClass));
    }
}
