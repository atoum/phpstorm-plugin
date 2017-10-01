package org.atoum.intellij.plugin.atoum;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public class MockTypeProvider implements PhpTypeProvider3 {

    @Override
    public char getKey() {
        return '\u0102';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (psiElement instanceof NewExpression) {
            NewExpression expr = (NewExpression) psiElement;
            ClassReference ref = expr.getClassReference();

            if (ref != null && ref.getFQN() != null && ref.getFQN().startsWith("\\mock\\")) {
                return new PhpType().add(ref.getFQN().substring("\\mock".length()));
            }
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
