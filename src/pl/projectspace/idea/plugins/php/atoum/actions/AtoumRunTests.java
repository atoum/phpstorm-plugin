package pl.projectspace.idea.plugins.php.atoum.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ScriptRunnerUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class AtoumRunTests extends AnAction {

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(false);
        event.getPresentation().setVisible(true);
        event.getPresentation().setText("atoum - run test");

        PhpClass currentTestClass = getCurrentTestClass(event);
        if (currentTestClass == null) {
            return;
        }

        event.getPresentation().setText("atoum - run test : " + currentTestClass.getName());
        event.getPresentation().setEnabled(true);
    }

    public void actionPerformed(final AnActionEvent e) {
        PhpClass currentTestClass = getCurrentTestClass(e);
        if (currentTestClass == null) {
            return;
        }

        Project project = e.getProject();

        ToolWindow toolWindow = getToolWindow(project);
        ConsoleView console = getConsole(toolWindow, project);

        String output = runTest(currentTestClass, project);

        toolWindow.setIcon(Icons.ATOUM);
        toolWindow.show(null);
        console.clear();
        console.print(output, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    protected String runTest(PhpClass currentTestClass, Project project) {
        final StringBuilder outputBuilder = new StringBuilder();
        String[] commandLineArgs = new String[1];
        commandLineArgs[0] = currentTestClass.getContainingFile().getVirtualFile().getPath();

        try {
            OSProcessHandler processHandler = ScriptRunnerUtil.execute(
                "./vendor/bin/atoum",
                    project.getBasePath(),
                null,
                commandLineArgs
            );

            processHandler.addProcessListener(new ProcessAdapter()
            {
                public void onTextAvailable(ProcessEvent event, Key outputType) {
                outputBuilder.append(event.getText());
                }
            });

            processHandler.startNotify();
            while (true)
            {
                boolean finished = processHandler.waitFor(1000L);
                if (finished) {
                    break;
                }
            }

            return outputBuilder.toString();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
            return "ERROR launching tests" + e1.getMessage();
        }
    }


    protected ToolWindow getToolWindow(Project project) {
        ToolWindow toolWindow;

        String windowId = "atoum";
        if (!Arrays.asList(ToolWindowManager.getInstance(project).getToolWindowIds()).contains(windowId)) {
            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(windowId, false, ToolWindowAnchor.BOTTOM);
        } else {
            toolWindow = ToolWindowManager.getInstance(project).getToolWindow(windowId);
        }

        return toolWindow;
    }

    protected ConsoleView getConsole(ToolWindow toolWindow, Project project) {
        ContentManager contentManager = toolWindow.getContentManager();
        Content logs;
        ConsoleView console;
        if (contentManager.getContents().length == 0) {
            TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
            console = builder.getConsole();
            logs = contentManager.getFactory().createContent(console.getComponent(), "Tests results", false);
            contentManager.addContent(logs);
        } else {
            logs = contentManager.getContents()[0];
            console = (ConsoleView)logs.getComponent();
        }

        return console;
    }

    @Nullable
    protected PhpClass getCurrentTestClass(AnActionEvent e) {
        Object psiFile = e.getData(PlatformDataKeys.PSI_FILE);

        if (null == psiFile) {
            return null;
        }

        if (!(psiFile instanceof PhpFile)) {
            return null;
        }
        PhpFile phpFile = ((PhpFile) psiFile);

        PhpClass currentClass = Utils.getFirstClassFromFile(phpFile);
        if (null == currentClass) {
            return null;
        }

        if (!Utils.isClassAtoumTest(currentClass)) {
            return Utils.locateTestClass(e.getProject(), currentClass);
        }

        return currentClass;
    }
}
