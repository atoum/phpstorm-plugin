package org.atoum.intellij.plugin.atoum.model;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.lang.psi.PhpFile;

public class RunnerConfiguration {

    protected PhpFile file;

    protected VirtualFile directory;

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

}
