package org.atoum.intellij.plugin.atoum.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestsResultFactory {


    public static TestsResult createFromTapOutput(String tapOutput)
    {
        TestsResult testsResult = new TestsResult();

        Pattern statusLinePattern = Pattern.compile("((?:not )?ok) (\\d+)(?: (?:# SKIP|# TODO|-) (.+)::(.+)\\(\\))?$");
        Pattern nameLinePattern = Pattern.compile("^# ([\\w\\\\]+)::(.+)\\(\\)$");

        String[] tapOutputLines = tapOutput.split("\n");

        Boolean infosFound = false;
        String currentMethodName = "";
        String currentContent = "";
        String currentClassname = "";
        String currentStatus = "";

        //The first line contains the number of tests
        for (Integer i = 1; i < tapOutputLines.length; i++) {
            String currentLine = tapOutputLines[i];

            Matcher statusLineMatcher = statusLinePattern.matcher(currentLine);

            if (statusLineMatcher.matches()) {
                if (infosFound) {
                    flushLine(testsResult, currentClassname, currentMethodName, currentContent, currentStatus);
                }

                currentMethodName = statusLineMatcher.group(4);
                currentContent = "";
                currentClassname = statusLineMatcher.group(3);
                currentStatus = statusLineMatcher.group(1);
                infosFound = true;

            } else {
                Matcher nameLineMatcher = nameLinePattern.matcher(currentLine);
                if (nameLineMatcher.matches()) {
                    currentClassname = nameLineMatcher.group(1);
                    currentMethodName = nameLineMatcher.group(2);
                } else {
                    currentContent += currentLine.substring(1) + "\n";
                }
            }
        }

        if (infosFound) {
            flushLine(testsResult, currentClassname, currentMethodName, currentContent, currentStatus);
        }

        return testsResult;
    }

    protected static void flushLine(TestsResult testsResult, String currentClassname, String currentMethodName, String currentContent, String currentStatus) {
        MethodResult methodResult = new MethodResult(currentMethodName, currentContent);
        if (!testsResult.hasClassResult(currentClassname)) {
            ClassResult classResult = new ClassResult();
            classResult.setName(currentClassname);
            testsResult.addClassResult(currentClassname, classResult);
        }

        if (currentStatus.equals("not ok")) {
            methodResult.definedStateFailed();
        } else if (currentStatus.equals("ok")) {
            methodResult.definedStatePassed();
        }

        testsResult.getClassResult(currentClassname).addMethodResult(methodResult);
    }
}
