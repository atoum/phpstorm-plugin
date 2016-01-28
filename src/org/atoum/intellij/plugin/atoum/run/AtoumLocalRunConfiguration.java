package org.atoum.intellij.plugin.atoum.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.run.PhpRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class AtoumLocalRunConfiguration extends PhpRunConfiguration implements LocatableConfiguration {


    public AtoumLocalRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    public boolean isGeneratedName() {
        return false;
    }

    @Nullable
    @Override
    public String suggestedName() {
        return null;
    }

    @NotNull
    @Override
    protected AtoumRunConfigurationSettings createSettings() {
        return new AtoumRunConfigurationSettings();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return null;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {

    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return null;
    }
}

