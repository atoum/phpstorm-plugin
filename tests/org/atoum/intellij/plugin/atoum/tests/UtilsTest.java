package org.atoum.intellij.plugin.atoum.tests;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.Utils;

import java.io.File;

public class UtilsTest extends LightCodeInsightFixtureTestCase {
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testGetFirstClassFromFile() {
        PhpClass phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestSimpleClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClass", phpClass.getFQN());

        phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestMultipleClassNotFirst.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\AnotherClassDefinedBeforeTheTest", phpClass.getFQN());
    }

    public void testGetFirstTestClassFromFile() {
        // Include stubs
        myFixture.copyFileToProject("atoum.php");

        PhpClass phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestSimpleClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClass", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestMultipleClassNotFirst.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestMultipleClassNotFirst", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestSimpleClassWithoutUse.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClassWithoutUse", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestWithParentClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestWithParentClass", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestWrongExtends.php"
        ));
        assertNull(phpClass);

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestWrongNamespace.php"
        ));
        assertNull(phpClass);

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestCustomNamespaceAnnotation.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\toto\\tata\\TestCustomNamespaceAnnotation", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestCustomNamespaceMethodCall.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\toto\\tata\\TestCustomNamespaceMethodCall", phpClass.getFQN());
    }

    public void testGetFirstTestClassFromFileWithoutStubs() {
        PhpClass phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestSimpleClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClass", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestMultipleClassNotFirst.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestMultipleClassNotFirst", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestSimpleClassWithoutUse.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClassWithoutUse", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestWithParentClass.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestWithParentClass", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestWrongExtends.php"
        ));
        assertNull(phpClass);

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestWrongNamespace.php"
        ));
        assertNull(phpClass);

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestCustomNamespaceAnnotation.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\toto\\tata\\TestCustomNamespaceAnnotation", phpClass.getFQN());

        phpClass = Utils.getFirstTestClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestCustomNamespaceMethodCall.php"
        ));
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\toto\\tata\\TestCustomNamespaceMethodCall", phpClass.getFQN());
    }

    public void testLocateTestClass()
    {
        // Load fake tests
        myFixture.copyDirectoryToProject("tests", "tests");

        PhpClass phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "src/TestSimpleClass.php"
        ));
        phpClass = Utils.locateTestClass(this.getProject(), phpClass);
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\tests\\units\\TestSimpleClass", phpClass.getFQN());

        phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "src/TestCustomNamespaceAnnotation.php"
        ));
        phpClass = Utils.locateTestClass(this.getProject(), phpClass);
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\toto\\tata\\TestCustomNamespaceAnnotation", phpClass.getFQN());

        phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "src/TestCustomNamespaceMethodCall.php"
        ));
        phpClass = Utils.locateTestClass(this.getProject(), phpClass);
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\toto\\tata\\TestCustomNamespaceMethodCall", phpClass.getFQN());

        phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "src/TestClassWithoutTest.php"
        ));
        phpClass = Utils.locateTestClass(this.getProject(), phpClass);
        assertNull(phpClass);
    }

    public void testLocateTestedClass()
    {
        // Load fake code
        myFixture.copyDirectoryToProject("src", "src");

        PhpClass phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestSimpleClass.php"
        ));
        phpClass = Utils.locateTestedClass(this.getProject(), phpClass);
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\TestSimpleClass", phpClass.getFQN());

        phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestCustomNamespaceAnnotation.php"
        ));
        phpClass = Utils.locateTestedClass(this.getProject(), phpClass);
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\TestCustomNamespaceAnnotation", phpClass.getFQN());

        phpClass = Utils.getFirstClassFromFile((PhpFile) myFixture.configureByFile(
            "tests/TestCustomNamespaceMethodCall.php"
        ));
        phpClass = Utils.locateTestedClass(this.getProject(), phpClass);
        assertNotNull(phpClass);
        assertEquals("\\PhpStormPlugin\\TestCustomNamespaceMethodCall", phpClass.getFQN());
    }
}
