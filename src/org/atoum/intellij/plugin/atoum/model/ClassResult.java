package org.atoum.intellij.plugin.atoum.model;

import java.util.ArrayList;

public class ClassResult
{

    public static String STATE_PASSED = "passed";
    public static String STATE_FAILED = "failed";
    public static String STATE_SKIPPED = "skipped";

    protected ArrayList<MethodResult> methodsResults;

    protected String fqn;

    public ClassResult() {
        this.methodsResults = new ArrayList<MethodResult>();
    }

    public void addMethodResult(MethodResult methodResult)
    {
        this.methodsResults.add(methodResult);
    }

    public String getState()
    {
        if (hasMethodOfState(MethodResult.STATE_FAILED)) {
            return STATE_FAILED;
        }

        if (hasMethodOfState(MethodResult.STATE_SKIPPED)) {
            return STATE_SKIPPED;
        }

        if (hasMethodOfState(MethodResult.STATE_PASSED)) {
            return STATE_PASSED;
        }

        return STATE_FAILED;
    }

    private boolean hasMethodOfState(String state) {
        for (MethodResult methodsResult : this.methodsResults) {
            if (methodsResult.getState().equals(state)) {
                return true;
            }
        }
        return false;
    }

    public Iterable<MethodResult> getMethods() {
        return this.methodsResults;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }
}
