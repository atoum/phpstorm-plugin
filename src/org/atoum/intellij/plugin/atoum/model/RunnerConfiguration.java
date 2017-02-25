package org.atoum.intellij.plugin.atoum.model;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;

public class RunnerConfiguration {

    protected PhpFile file;

    protected VirtualFile directory;

    protected Method method;

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

    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getMethod () {
        return method;
    }
}
