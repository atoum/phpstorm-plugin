package org.atoum.intellij.plugin.atoum.run;

import com.intellij.util.xmlb.annotations.Property;
import com.jetbrains.php.phpunit.PhpUnitTestRunnerSettings;
import com.jetbrains.php.run.PhpCommandLineSettings;
import com.jetbrains.php.run.PhpRunConfigurationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AtoumRunConfigurationSettings implements PhpRunConfigurationSettings {
    @Nullable
    public String getWorkingDirectory() {
        return "";
    }

    public void setWorkingDirectory(@NotNull String workingDirectory) {
    }
}
