package org.atoum.intellij.plugin.atoum.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestsResultFactory {


    public static TestsResult createFromTapOutput(String tapOutput)
    {
        TestsResult testsResult = new TestsResult();

        Pattern statusLinePattern = Pattern.compile("((?:not )?ok) (\\d+)(?: (?:# SKIP|# TODO|-) (.+)::(.+)\\(\\))?$");
        Pattern nameLinePattern = Pattern.compile("^# ([\\w\\\\]+)::(.+)\\(\\)$");
        Pattern planLinePattern = Pattern.compile("^\\d+\\.\\.\\d+$");
        Pattern failLocationPattern = Pattern.compile("^# .+:([0-9]+)$");

        String[] tapOutputLines = tapOutput.split("\n");

        Boolean infosFound = false;
        String currentMethodName = "";
        String currentContent = "";
        String currentClassname = "";
        String currentStatus = "";
        Integer currentLineNumber = null;

        for (Integer i = 0; i < tapOutputLines.length; i++) {
            String currentLine = tapOutputLines[i];

            if (i == 0 && planLinePattern.matcher(currentLine).matches()) {
                continue;
            }

            Matcher statusLineMatcher = statusLinePattern.matcher(currentLine);

            if (statusLineMatcher.matches()) {
                if (infosFound) {
                    flushLine(testsResult, currentClassname, currentMethodName, currentContent, currentStatus, currentLineNumber);
                }

                currentMethodName = statusLineMatcher.group(4);
                currentContent = "";
                currentClassname = statusLineMatcher.group(3);
                currentStatus = statusLineMatcher.group(1);
                currentLineNumber = null;
                infosFound = true;

            } else {
                Matcher nameLineMatcher = nameLinePattern.matcher(currentLine);
                if (nameLineMatcher.matches()) {
                    currentClassname = nameLineMatcher.group(1);
                    currentMethodName = nameLineMatcher.group(2);
                } else {
                    Matcher failLocationMatcher = failLocationPattern.matcher(currentLine);
                    if (failLocationMatcher.matches()) {
                        currentLineNumber = Integer.parseInt(failLocationMatcher.group(1));
                    } else if (currentLine.length() > 0) {
                        currentContent += currentLine.substring(1) + "\n";
                    }
                }
            }
        }

        if (infosFound) {
            flushLine(testsResult, currentClassname, currentMethodName, currentContent, currentStatus, currentLineNumber);
        }

        return testsResult;
    }

    protected static void flushLine(TestsResult testsResult, String currentClassname, String currentMethodName, String currentContent, String currentStatus, Integer lineNumber) {
        MethodResult methodResult = new MethodResult(currentMethodName, currentContent, lineNumber);
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
