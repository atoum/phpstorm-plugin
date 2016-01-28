package org.atoum.intellij.plugin.atoum.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.run.PhpRunConfigurationFactoryBase;
import org.jetbrains.annotations.NotNull;
import pl.projectspace.idea.plugins.php.atoum.actions.Icons;

import javax.swing.*;

public class AtoumLocalRunConfigurationType implements ConfigurationType {
    private final ConfigurationFactory myFactory = new PhpRunConfigurationFactoryBase(this) {
        public RunConfiguration createTemplateConfiguration(Project project) {
            return new AtoumLocalRunConfiguration(project, this, "");
        }
    };

    public AtoumLocalRunConfigurationType() {
    }

    public static AtoumLocalRunConfigurationType getInstance() {
        return (AtoumLocalRunConfigurationType) ConfigurationTypeUtil.findConfigurationType(AtoumLocalRunConfigurationType.class);
    }

    public String getDisplayName() {
        return "atoum";
    }

    public String getConfigurationTypeDescription() {
        return "Launch your atoum unit tests";
    }

    public Icon getIcon() {
        return Icons.ATOUM;
    }

    @NotNull
    public String getId() {
        return "AtoumRunConfigurationType";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{this.myFactory};
    }
}
