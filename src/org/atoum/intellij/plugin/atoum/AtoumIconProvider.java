
package org.atoum.intellij.plugin.atoum;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpFile;
import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;

public class AtoumIconProvider extends IconProvider {

    public Icon getIcon(@NotNull PsiElement element, @IconFlags int flags) {
        if (element instanceof PhpFile) {
            if (Utils.getFirstTestClassFromFile((PhpFile) element) != null) {
                return Icons.ATOUM_FILE;
            }
        }

        return null;
    }
}
