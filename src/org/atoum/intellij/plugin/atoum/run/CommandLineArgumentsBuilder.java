package org.atoum.intellij.plugin.atoum.run;

import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandLineArgumentsBuilder {

    ArrayList<String> commandLineArgs;
    String baseDir;

    public CommandLineArgumentsBuilder(String atoumPath, String baseDir)
    {
        this.baseDir = baseDir;
        this.commandLineArgs = new ArrayList<String>();
        this.commandLineArgs.add(this.relativizePath(atoumPath));
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
            this.commandLineArgs.add(this.relativizePath(runnerConfiguration.getDirectory().getPath()));
        }
        if (null != runnerConfiguration.getFile()) {
            this.commandLineArgs.add("-f");
            this.commandLineArgs.add(this.relativizePath(runnerConfiguration.getFile().getVirtualFile().getPath()));
        }

        return this;
    }

    public String[] build()
    {
        return Arrays.copyOf(this.commandLineArgs.toArray(), this.commandLineArgs.size(), String[].class);
    }

    protected String relativizePath(String path)
    {
        return new File(this.baseDir).toURI().relativize(new File(path).toURI()).getPath();
    }
}
