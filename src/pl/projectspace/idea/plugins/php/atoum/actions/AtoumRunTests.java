package pl.projectspace.idea.plugins.php.atoum.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.*;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.TestFrameworkRunningModel;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.TestResultsViewer;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.run.PhpRunConfiguration;
import com.jetbrains.php.run.PhpRunConfigurationFactoryBase;
import org.atoum.intellij.plugin.atoum.run.AtoumLocalRunConfiguration;
import org.atoum.intellij.plugin.atoum.run.AtoumLocalRunConfigurationType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
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
        getConsole(toolWindow, project, currentTestClass);

        toolWindow.setIcon(Icons.ATOUM);
        toolWindow.show(null);
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

    protected BaseTestsOutputConsoleView getConsole(ToolWindow toolWindow, Project project, final PhpClass currentTestClass) {

        ConfigurationFactory myFactory = new PhpRunConfigurationFactoryBase(new AtoumLocalRunConfigurationType()) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new AtoumLocalRunConfiguration(project, this, "");
            }
        };

        String[] commandLineArgs = new String[2];
        commandLineArgs[0] = currentTestClass.getContainingFile().getVirtualFile().getPath();
        commandLineArgs[1] = "--use-tap-report";
        VirtualFile testBaseDir = findTestBaseDir(currentTestClass, project);


        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        PhpRunConfiguration runConfiguration = new AtoumLocalRunConfiguration(project, myFactory, "test");
        TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(runConfiguration, "atoum", executor);
        BaseTestsOutputConsoleView testsOutputConsoleView = SMTestRunnerConnectionUtil.createConsole("atoumConsole", testConsoleProperties);

        Disposer.register(project, testsOutputConsoleView);

        ContentManager contentManager = toolWindow.getContentManager();
        Content myContent;
        myContent = toolWindow.getContentManager().getFactory().createContent(testsOutputConsoleView.getComponent(), "tests results", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(myContent);

        final SMTRunnerConsoleView console = (SMTRunnerConsoleView)testsOutputConsoleView;

        OSProcessHandler processHandler = null;
        try {
            processHandler = ScriptRunnerUtil.execute(
                    findAtoumBinPath(testBaseDir),
                    testBaseDir.getPath(),
                    null,
                    commandLineArgs
            );


            testsOutputConsoleView.attachToProcess(processHandler);
            console.getResultsViewer().setAutoscrolls(true);
            TestConsoleProperties.HIDE_PASSED_TESTS.set(testsOutputConsoleView.getProperties(), false);
            TestConsoleProperties.SELECT_FIRST_DEFECT.set(testsOutputConsoleView.getProperties(), true);

            final OSProcessHandler finalProcessHandler = processHandler;
            console.getResultsViewer().addEventsListener(new TestResultsViewer.EventsListener() {

                final StringBuilder outputBuilder = new StringBuilder();

                @Override
                public void onTestingStarted(TestResultsViewer testResultsViewer) {

                    if (finalProcessHandler != null) {
                        finalProcessHandler.addProcessListener(new ProcessAdapter() {
                            public void onTextAvailable(ProcessEvent event, Key outputType) {
                                outputBuilder.append(event.getText());
                            }
                            public void processTerminated(ProcessEvent event) {
                            }
                        });
                    }
                }

                @Override
                public void onTestingFinished(TestResultsViewer testResultsViewer) {
                    String tapOutput = outputBuilder.toString();

                    SMTestProxy.SMRootTestProxy class1TestProxy = new SMTestProxy.SMRootTestProxy();
                    class1TestProxy.setRootLocationUrl(currentTestClass.getContainingFile().getVirtualFile().getPath());
                    class1TestProxy.setPresentation(currentTestClass.getFQN());
                    class1TestProxy.setFinished();

                    Boolean firstTestFound = false;
                    Boolean currentTestIsOk = false;
                    String testContent = "";
                    String testName = "";
                    String[] tapOutputLines = tapOutput.split("\n");
                    for (Integer i = 0; i < tapOutputLines.length; i++) {
                        testContent += tapOutputLines[i] + "\n";

                        if (tapOutputLines[i].startsWith("not ok") || tapOutputLines[i].startsWith("ok")) {
                            if (firstTestFound) {
                                SMTestProxy method1TestProxy = new SMTestProxy(testName, true, "");
                                if (currentTestIsOk) {
                                    method1TestProxy.addSystemOutput(testContent);
                                } else {
                                    method1TestProxy.setTestFailed(testName + " Failed", testContent, true);
                                }
                                method1TestProxy.setFinished();
                                class1TestProxy.addChild(method1TestProxy);
                                if (method1TestProxy.isDefect()) {
                                    class1TestProxy.setTestFailed("", "", true);
                                }
                            }

                            if (tapOutputLines[i].startsWith("ok") && i +1 < tapOutputLines.length ) {
                                testName = tapOutputLines[i + 1].substring(tapOutputLines[i + 1].indexOf("::") + 2);
                            } else {
                                testName = tapOutputLines[i].substring(tapOutputLines[i].indexOf("::") + 2);
                            }
                            testContent = "";
                            firstTestFound = true;
                            currentTestIsOk = tapOutputLines[i].startsWith("ok");

                        }

                    }

                    if (firstTestFound) {
                        SMTestProxy method1TestProxy = new SMTestProxy(testName, true, "");
                        method1TestProxy.setTestFailed(testName + " Failed", testContent, true);
                        method1TestProxy.setFinished();
                        class1TestProxy.addChild(method1TestProxy);
                    }

                    console.getResultsViewer().getTestsRootNode().addChild(class1TestProxy);
                }

                @Override
                public void onTestNodeAdded(TestResultsViewer testResultsViewer, SMTestProxy smTestProxy) {

                }

                @Override
                public void onSelected(@Nullable SMTestProxy smTestProxy, @NotNull TestResultsViewer testResultsViewer, @NotNull TestFrameworkRunningModel testFrameworkRunningModel) {

                }
            });

            processHandler.startNotify();


        } catch (ExecutionException e) {
        }

        return testsOutputConsoleView;
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
