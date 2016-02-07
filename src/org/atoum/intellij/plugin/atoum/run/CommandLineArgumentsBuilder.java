package org.atoum.intellij.plugin.atoum.run;

import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandLineArgumentsBuilder {

    ArrayList<String> commandLineArgs;

    public CommandLineArgumentsBuilder()
    {
        this.commandLineArgs = new ArrayList<String>();
    }

    public CommandLineArgumentsBuilder useTapReport()
    {
        this.commandLineArgs.add("--use-tap-report");

        return this;
    }

    public CommandLineArgumentsBuilder useConfiguration(RunnerConfiguration runnerConfiguration)
    {
        if (null != runnerConfiguration.getDirectory()) {
            this.commandLineArgs.add("-d");
            this.commandLineArgs.add(runnerConfiguration.getDirectory().getPath());
        }
        if (null != runnerConfiguration.getFile()) {
            this.commandLineArgs.add("-f");
            this.commandLineArgs.add(runnerConfiguration.getFile().getVirtualFile().getPath());
        }

        return this;
    }

    public String[] build()
    {
        return Arrays.copyOf(this.commandLineArgs.toArray(), this.commandLineArgs.size(), String[].class);
    }

}
