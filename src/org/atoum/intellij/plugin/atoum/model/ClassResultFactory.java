package org.atoum.intellij.plugin.atoum.model;

public class ClassResultFactory {

    public static ClassResult createFromTapOutput(String tapOutput)
    {
        ClassResult classResult = new ClassResult();

        Boolean firstTestFound = false;
        Boolean currentTestIsOk = false;
        String testContent = "";
        String testName = "";
        String[] tapOutputLines = tapOutput.split("\n");
        for (Integer i = 0; i < tapOutputLines.length; i++) {
            testContent += tapOutputLines[i] + "\n";

            if (tapOutputLines[i].startsWith("not ok") || tapOutputLines[i].startsWith("ok")) {
                if (firstTestFound) {

                    MethodResult methodResult = new MethodResult(testName, testContent);

                    if (currentTestIsOk) {
                        methodResult.definedStatePassed();
                    } else {
                        methodResult.definedStateFailed();
                    }
                    classResult.addMethodResult(methodResult);
                }

                if (tapOutputLines[i].startsWith("ok") && i +1 < tapOutputLines.length ) {
                    testName = tapOutputLines[i + 1].substring(tapOutputLines[i + 1].indexOf("::") + 2);
                } else {
                    testName = tapOutputLines[i].substring(tapOutputLines[i].indexOf("::") + 2);
                }
                testContent = "";
                firstTestFound = true;
                currentTestIsOk = tapOutputLines[i].startsWith("ok");
            }

        }

        if (firstTestFound) {
            MethodResult methodResult = new MethodResult(testName, testContent);
            classResult.addMethodResult(methodResult);
        }

        return classResult;
    }
}
