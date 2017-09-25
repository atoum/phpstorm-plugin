package org.atoum.intellij.plugin.atoum.tests;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.Utils;

import java.io.File;

public class UtilsTest extends LightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("atoum.php");
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testGetFirstClassFromFile() {
        PhpClass phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "TestSimpleClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClass", phpClass.getFQN());

        phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "TestMultipleClassNotFirst.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\AnotherClassDefinedBeforeTheTest", phpClass.getFQN());
    }

    public void testGetFirstTestClassFromFile() {
        PhpClass phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "TestSimpleClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClass", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "TestMultipleClassNotFirst.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestMultipleClassNotFirst", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "TestSimpleClassWithoutUse.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClassWithoutUse", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "TestWithParentClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestWithParentClass", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "TestWrongExtends.php"
        ));
        assertNull(phpClass);

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "TestWrongNamespace.php"
        ));
        assertNull(phpClass);
    }
}
