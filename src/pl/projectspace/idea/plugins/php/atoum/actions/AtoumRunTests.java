package pl.projectspace.idea.plugins.php.atoum.actions;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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


        VirtualFile testBaseDir = findTestBaseDir(currentTestClass, project);

        ProcessBuilder ps = new ProcessBuilder(findAtoumBinPath(testBaseDir), "--use-tap-report", "-f ", currentTestClass.getContainingFile().getVirtualFile().getPath());
        ps.directory(new File(testBaseDir.getPath()));

        //From the DOC:  Initially, this property is false, meaning that the
        //standard output and error output of a subprocess are sent to two
        //separate streams
        ps.redirectErrorStream(true);


        try {
            Process pr = ps.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String content = "";
            String line;
            while ((line = in.readLine()) != null) {
                content += line + "\n";
            }
            pr.waitFor();

            in.close();
            if (0 == pr.exitValue()) {
                return content;
            }
            return "ERROR " + content;
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR launching tests : " + e.getMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "ERROR launching tests : " + e.getMessage();
        }
    }

    protected VirtualFile findTestBaseDir(PhpClass currentTestClass, Project project)
    {
        Boolean continueSearch = true;
        Integer maxDirs = 35;
        Integer dirCount = 0;
        PsiDirectory currentDir = currentTestClass.getContainingFile().getContainingDirectory();
        while (continueSearch) {
            dirCount++;
            if (currentDir.getVirtualFile().equals(project.getBaseDir())) {
                continueSearch = false;
            } else if (dirCount >= maxDirs) {
                continueSearch = false;
            } else {
                if (new File(currentDir.getVirtualFile().getPath() + "/composer.json").exists()) {
                    return currentDir.getVirtualFile();
                }
            }
            currentDir = currentDir.getParentDirectory();
            if (null == currentDir) {
                return project.getBaseDir();
            }
        }

        return project.getBaseDir();
    }

    protected String findAtoumBinPath(VirtualFile dir)
    {
        String defaultBinPath = dir.getPath() + "/vendor/bin/atoum";

        String binDir = getComposerBinDir(dir.getPath() + "/composer.json");
        String binPath = dir.getPath() + "/" + binDir + "/atoum";
        if (null != binDir && new File(binPath).exists()) {
            return binPath;
        }

        return defaultBinPath;
    }

    @Nullable
    protected String getComposerBinDir(String composerPath) {
        try {
            String composerJsonContent = new String(Files.readAllBytes(Paths.get(composerPath)));
            JSONObject obj = new JSONObject(composerJsonContent);
            return obj.getJSONObject("config").get("bin-dir").toString();
        } catch (JSONException e) {
            return null;
        } catch (IOException e) {
            return null;
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
