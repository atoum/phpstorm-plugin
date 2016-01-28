package org.atoum.intellij.plugin.atoum.model;


import java.util.HashMap;

public class TestsResult {

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

}
