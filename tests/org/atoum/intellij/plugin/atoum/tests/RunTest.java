package org.atoum.intellij.plugin.atoum.tests;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.atoum.intellij.plugin.atoum.actions.Run;

import java.io.File;

public class RunTest extends LightCodeInsightFixtureTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Add Fixtures
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("TestWithMethods.php"));
    }

    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testGetCurrentTestMethod() {
        Run action = new Run();
        TestActionEvent e = new TestActionEvent(action);
        CaretModel cursor = CommonDataKeys.EDITOR.getData(e.getDataContext()).getCaretModel();

        // Ensure cursor is at position 0
        cursor.moveToOffset(0);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run test : TestWithMethods", e.getPresentation().getText());

        // Move cursor on the class declaration
        cursor.moveToOffset(190);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run test : TestWithMethods", e.getPresentation().getText());

        // Move cursor on method declaration: beforeTestMethod
        cursor.moveToOffset(230);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run test : TestWithMethods", e.getPresentation().getText());

        // Move cursor in method body: beforeTestMethod
        cursor.moveToOffset(345);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run test : TestWithMethods", e.getPresentation().getText());

        // Move cursor on method declaration: test__construct_bad
        cursor.moveToOffset(420);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run TestWithMethods::test__construct_bad", e.getPresentation().getText());

        // Move cursor in method body: test__construct_bad
        cursor.moveToOffset(580);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run TestWithMethods::test__construct_bad", e.getPresentation().getText());

        // Move cursor on method declaration: test__construct_ok
        cursor.moveToOffset(740);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run TestWithMethods::test__construct_ok", e.getPresentation().getText());

        // Move cursor in method body: test__construct_ok
        cursor.moveToOffset(815);
        action.update(e);
        assertTrue(e.getPresentation().isEnabledAndVisible());
        assertEquals("atoum - run TestWithMethods::test__construct_ok", e.getPresentation().getText());
    }
}
