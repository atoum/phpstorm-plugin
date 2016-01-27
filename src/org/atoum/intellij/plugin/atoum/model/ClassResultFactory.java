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

                Integer lineWithMethod = i;

                if (tapOutputLines[i].startsWith("ok") && i +1 < tapOutputLines.length ) {
                    lineWithMethod = i + 1;

                }

                testName = tapOutputLines[lineWithMethod].substring(tapOutputLines[lineWithMethod].indexOf("::") + 2);
                //only works with one classe per file
                classResult.setName(tapOutputLines[lineWithMethod].substring(1, tapOutputLines[lineWithMethod].indexOf("::")));

                testContent = "";
                firstTestFound = true;
                currentTestIsOk = tapOutputLines[i].startsWith("ok");
            }

        }

        if (firstTestFound) {
            MethodResult methodResult = new MethodResult(testName, testContent);
            if (currentTestIsOk) {
                methodResult.definedStatePassed();
            } else {
                methodResult.definedStateFailed();
            }
            classResult.addMethodResult(methodResult);
        }

        return classResult;
    }
}
