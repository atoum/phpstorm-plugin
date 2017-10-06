package org.atoum.intellij.plugin.atoum.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
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
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.config.interpreters.PhpConfigurationOptionData;
import com.jetbrains.php.config.interpreters.PhpInterpreter;
import com.jetbrains.php.run.PhpRunConfiguration;
import com.jetbrains.php.run.PhpRunConfigurationFactoryBase;
import org.atoum.intellij.plugin.atoum.AtoumUtils;
import org.atoum.intellij.plugin.atoum.actions.RerunFailedTestsAction;
import org.atoum.intellij.plugin.atoum.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.atoum.intellij.plugin.atoum.Icons;
import java.io.File;

import java.util.*;

public class Runner {

    protected Project project;

    public Runner(Project project)
    {
        this.project = project;
    }

    public void run(RunnerConfiguration runnerConfiguration)
    {
        ToolWindow toolWindow = getToolWindow();
        getConsole(toolWindow, project, runnerConfiguration);
    }

    public ToolWindow getToolWindow() {
        ToolWindow toolWindow;

        String windowId = "atoum";
        if (!Arrays.asList(ToolWindowManager.getInstance(project).getToolWindowIds()).contains(windowId)) {
            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(windowId, false, ToolWindowAnchor.BOTTOM);
        } else {
            toolWindow = ToolWindowManager.getInstance(project).getToolWindow(windowId);
        }

        toolWindow.setIcon(Icons.ATOUM);
        toolWindow.show(null);

        return toolWindow;
    }

    // like
    //   https://github.com/JetBrains/intellij-community/blob/6555e34dc0b5c4bd3a8c9efc7d8e5ca84929af40/platform/platform-impl/src/com/intellij/execution/process/ScriptRunnerUtil.java
    // with not log and charset support
    // but with environment variables support
    protected OSProcessHandler prepareProcessHandler(@NotNull String exePath, @Nullable String workingDirectory, String[] parameters, @Nullable Map<String, String> environment) throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine(exePath);
        commandLine.addParameters(parameters);
        if (workingDirectory != null) {
            commandLine.setWorkDirectory(workingDirectory);
        }

        commandLine.withEnvironment(environment);

        return new ColoredProcessHandler(commandLine);
    }

    public BaseTestsOutputConsoleView getConsole(ToolWindow toolWindow, Project project, final RunnerConfiguration runnerConfiguration) {

        ConfigurationFactory myFactory = new PhpRunConfigurationFactoryBase(new AtoumLocalRunConfigurationType()) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new AtoumLocalRunConfiguration(project, this, "");
            }
        };

        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        PhpRunConfiguration runConfiguration = new AtoumLocalRunConfiguration(project, myFactory, "test");
        TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(runConfiguration, "atoum", executor);
        final BaseTestsOutputConsoleView testsOutputConsoleView = SMTestRunnerConnectionUtil.createConsole("atoumConsole", testConsoleProperties);

        Disposer.register(project, testsOutputConsoleView);

        VirtualFile testBaseDir = null;
        try {
            testBaseDir = AtoumUtils.findTestBaseDir(runnerConfiguration, project);
        } catch (Exception e) {
            testBaseDir = project.getBaseDir();
        }

        String testBasePath = testBaseDir.getPath();
        String atoumBinPath = AtoumUtils.findAtoumBinPath(testBaseDir);

        String phpPath = "php";
        PhpInterpreter interpreter = PhpProjectConfigurationFacade.getInstance(project).getInterpreter();
        if (null != interpreter) {
            phpPath = interpreter.getPathToPhpExecutable();
        }

        List<PhpConfigurationOptionData> phpConfig;
        try {
            phpConfig = interpreter.getConfigurationOptions();
        } catch (NullPointerException e) {
            phpConfig = new ArrayList<PhpConfigurationOptionData>();
        }

        CommandLineArgumentsBuilder commandLineBuilder = (new CommandLineArgumentsBuilder(atoumBinPath, testBasePath, phpConfig))
            .useTapReport()
            .useConfiguration(runnerConfiguration)
        ;

        String phpstormConfigFile = testBasePath + "/.atoum.phpstorm.php";
        if (new File(phpstormConfigFile).exists()) {
            commandLineBuilder.useConfigFile(phpstormConfigFile);
        }

        ActionManager am = ActionManager.getInstance();
        DefaultActionGroup buttonGroup = new DefaultActionGroup();
        ActionToolbar viewToolbar = am.createActionToolbar("atoum.ConsoleToolbar", buttonGroup, false);

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(false, true);
        toolWindowPanel.setContent(testsOutputConsoleView.getComponent());
        toolWindowPanel.setToolbar(viewToolbar.getComponent());

        ContentManager contentManager = toolWindow.getContentManager();
        Content myContent = contentManager.getFactory().createContent(toolWindowPanel.getComponent(), "tests results", false);
        contentManager.removeAllContents(true);
        contentManager.addContent(myContent);

        final SMTRunnerConsoleView console = (SMTRunnerConsoleView)testsOutputConsoleView;

        buttonGroup.add(new RerunFailedTestsAction(console.getResultsViewer().getTestsRootNode()));

        String[] commandLineArgs = commandLineBuilder.build();

        HashMap environnmentVariables = new HashMap();
        environnmentVariables.put("PHPSTORM", "1");

        OSProcessHandler processHandler = null;
        try {
            processHandler = this.prepareProcessHandler(
                phpPath,
                testBasePath,
                commandLineArgs,
                environnmentVariables
            );


            testsOutputConsoleView.attachToProcess(processHandler);
            console.getResultsViewer().setAutoscrolls(true);
            TestConsoleProperties.HIDE_PASSED_TESTS.set(testsOutputConsoleView.getProperties(), true);
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
                    SMTestProxy testsRootNode = testResultsViewer.getTestsRootNode();

                    if (outputBuilder.length() == 0) {
                        testsRootNode.setTestFailed("No tests were found!", "", true);
                        return;
                    }

                    TestsResult testsResult = TestsResultFactory.createFromTapOutput(outputBuilder.toString());

                    SMTRootTestProxyFactory.updateFromTestResult(testsResult, testsRootNode);

                    selectFirstFailedMethod();

                    if (testsResult.getState().equals(testsResult.STATE_PASSED)) {
                        TestConsoleProperties.HIDE_PASSED_TESTS.set(testsOutputConsoleView.getProperties(), false);
                    }
                }

                protected void selectFirstFailedMethod()
                {
                    for (SMTestProxy testProxy: console.getResultsViewer().getTestsRootNode().getAllTests()) {
                        for (SMTestProxy methodProxy: testProxy.getAllTests()) {
                            if (methodProxy.isDefect()) {
                                console.getResultsViewer().selectAndNotify(methodProxy);
                            }
                        }

                    }
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
            Notifications.Bus.notify(new Notification("atoumGroup", "Error running tests", e.getMessage(), NotificationType.ERROR, null), project);
        }

        return testsOutputConsoleView;
    }
}
