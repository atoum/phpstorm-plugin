package org.atoum.intellij.plugin.atoum.model;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;

import java.util.ArrayList;
import java.util.List;

public class RunnerConfiguration {

    protected PhpFile file;

    protected VirtualFile directory;

    protected List<Method> methods = new ArrayList<>();

    public PhpFile getFile () {
        return file;
    }

    public void setFile(PhpFile file) {
        this.file = file;
    }

    public VirtualFile getDirectory () {
        return directory;
    }

    public void setDirectory(VirtualFile directory) {
        this.directory = directory;
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public List<Method> getMethods () {
        return methods;
    }
}
