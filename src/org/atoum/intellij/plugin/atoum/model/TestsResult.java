package org.atoum.intellij.plugin.atoum.model;


import java.util.HashMap;

public class TestsResult {

    public static String STATE_PASSED = "passed";
    public static String STATE_FAILED = "failed";
    public static String STATE_SKIPPED = "skipped";


    protected HashMap<String, ClassResult> classResults;

    public TestsResult()
    {
        this.classResults = new HashMap<String, ClassResult>();
    }

    public ClassResult getClassResult(String name)
    {
        return this.classResults.get(name);
    }

    public void addClassResult(String name, ClassResult classResult)
    {
        this.classResults.put(name, classResult);
    }

    public boolean hasClassResult(String name)
    {
        return this.classResults.containsKey(name);
    }

    public Iterable<ClassResult> getClassResults()
    {
        return this.classResults.values();
    }

    public String getState()
    {
        if (hasClassOfstate(MethodResult.STATE_FAILED)) {
            return STATE_FAILED;
        }

        if (hasClassOfstate(MethodResult.STATE_SKIPPED)) {
            return STATE_SKIPPED;
        }

        if (hasClassOfstate(MethodResult.STATE_PASSED)) {
            return STATE_PASSED;
        }

        return STATE_FAILED;
    }

    private boolean hasClassOfstate(String state) {
        for (ClassResult classResult : this.getClassResults()) {
            if (classResult.getState().equals(state)) {
                return true;
            }
        }
        return false;
    }
}
