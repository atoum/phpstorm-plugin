package org.atoum.intellij.plugin.atoum.run;

import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import org.atoum.intellij.plugin.atoum.model.ClassResult;
import org.atoum.intellij.plugin.atoum.model.MethodResult;
import org.atoum.intellij.plugin.atoum.model.TestsResult;

public class SMTRootTestProxyFactory {

    public static SMTestProxy.SMRootTestProxy createFromClassResult(ClassResult classResult) {
        SMTestProxy.SMRootTestProxy classNode = new SMTestProxy.SMRootTestProxy();
        classNode.setPresentation(classResult.getName());
        classNode.setFinished();

        if (classResult.getState().equals(ClassResult.STATE_FAILED)) {
            classNode.setTestFailed("", "", true);
        }

        for (MethodResult methodsResult: classResult.getMethods()) {
            SMTestProxy methodNode = new SMTestProxy(methodsResult.getName(), true, "");

            if (methodsResult.getState().equals(MethodResult.STATE_FAILED)) {
                methodNode.setTestFailed(methodsResult.getName() + " Failed", methodsResult.getContent(), true);
            } else if (methodsResult.getState().equals(MethodResult.STATE_PASSED)) {
                methodNode.addSystemOutput(methodsResult.getContent());
            }

            methodNode.setFinished();
            classNode.addChild(methodNode);
        }

        return classNode;
    }

    public static void updateFromTestResult(TestsResult testsResult, SMTestProxy testsRootNode) {
        for (ClassResult classResult: testsResult.getClassResults()) {
            testsRootNode.addChild(SMTRootTestProxyFactory.createFromClassResult(classResult));
        }

        if (testsResult.getState().equals(TestsResult.STATE_FAILED)) {
            testsRootNode.setTestFailed("", "", true);
        }
    }

}
