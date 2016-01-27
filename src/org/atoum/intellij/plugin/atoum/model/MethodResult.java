package org.atoum.intellij.plugin.atoum.model;

public class MethodResult {

    public static String STATE_PASSED = "passed";
    public static String STATE_FAILED = "failed";
    public static String STATE_SKIPPED = "skipped";

    protected String state = STATE_FAILED;

    protected String name;
    protected String content;

    public MethodResult(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public void definedStatePassed() {
        this.state = STATE_PASSED;
    }

    public void definedStateFailed() {
        this.state = STATE_FAILED;
    }

    public void definedStateSkipped() {
        this.state = STATE_SKIPPED;
    }

    public String getName()
    {
        return this.name;
    }

    public String getContent()
    {
        return this.content;
    }

    public String getState()
    {
        return this.state;
    }
}
