package org.atoum.intellij.plugin.atoum.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ScriptRunnerUtil;
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
import com.jetbrains.php.run.PhpRunConfiguration;
import com.jetbrains.php.run.PhpRunConfigurationFactoryBase;
import org.atoum.intellij.plugin.atoum.AtoumUtils;
import org.atoum.intellij.plugin.atoum.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.atoum.intellij.plugin.atoum.Icons;

import java.util.ArrayList;
import java.util.Arrays;

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

    public BaseTestsOutputConsoleView getConsole(ToolWindow toolWindow, Project project, final RunnerConfiguration runnerConfiguration) {

        ConfigurationFactory myFactory = new PhpRunConfigurationFactoryBase(new AtoumLocalRunConfigurationType()) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new AtoumLocalRunConfiguration(project, this, "");
            }
        };

        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        PhpRunConfiguration runConfiguration = new AtoumLocalRunConfiguration(project, myFactory, "test");
        TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(runConfiguration, "atoum", executor);
        BaseTestsOutputConsoleView testsOutputConsoleView = SMTestRunnerConnectionUtil.createConsole("atoumConsole", testConsoleProperties);

        Disposer.register(project, testsOutputConsoleView);

        String[] commandLineArgs = (new CommandLineArgumentsBuilder()).useTapReport().useConfiguration(runnerConfiguration).build();

        VirtualFile testBaseDir = null;
        try {
            testBaseDir = AtoumUtils.findTestBaseDir(runnerConfiguration, project);
        } catch (Exception e) {
            return testsOutputConsoleView;
        }


        ContentManager contentManager = toolWindow.getContentManager();
        Content myContent;
        myContent = toolWindow.getContentManager().getFactory().createContent(testsOutputConsoleView.getComponent(), "tests results", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(myContent);

        final SMTRunnerConsoleView console = (SMTRunnerConsoleView)testsOutputConsoleView;

        OSProcessHandler processHandler = null;
        try {
            processHandler = ScriptRunnerUtil.execute(
                AtoumUtils.findAtoumBinPath(testBaseDir),
                testBaseDir.getPath(),
                null,
                commandLineArgs
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
                    TestsResult testsResult = TestsResultFactory.createFromTapOutput(outputBuilder.toString());
                    SMTestProxy testsRootNode = console.getResultsViewer().getTestsRootNode();

                    SMTRootTestProxyFactory.updateFromTestResult(testsResult, testsRootNode);

                    selectFirstFailedMethod();
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
