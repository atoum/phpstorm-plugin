package org.atoum.intellij.plugin.atoum.tests;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.Utils;

import java.io.File;

public class UtilsTest extends LightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Add Fixtures
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("TestA.php"));
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testGetFirstClassFromFile() throws Exception {
        PsiFile psiElement = myFixture.getFile();
        PhpClass phpClass = Utils.getFirstClassFromFile((PhpFile) psiElement);

        assertNotNull(phpClass);

        assertEquals("\\PhpStormPlugin\\tests\\units\\TestA", phpClass.getFQN());
    }

}
