
package org.atoum.intellij.plugin.atoum;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpFile;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import pl.projectspace.idea.plugins.php.atoum.actions.Icons;
import pl.projectspace.idea.plugins.php.atoum.actions.Utils;

public class AtoumIconProvider extends IconProvider {

    public Icon getIcon(@NotNull PsiElement element, @IconFlags int flags) {
        if (element instanceof PhpFile) {
            if (Utils.isClassAtoumTest(Utils.getFirstClassFromFile((PhpFile) element))) {
                return Icons.ATOUM_FILE;
            }
        }

        return null;
    }
}
