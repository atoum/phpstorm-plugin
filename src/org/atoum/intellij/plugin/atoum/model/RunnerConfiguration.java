package org.atoum.intellij.plugin.atoum.model;

import com.jetbrains.php.lang.psi.PhpFile;

public class RunnerConfiguration {

    protected PhpFile file;

    public PhpFile getFile () {
        return file;
    }

    public void setFile(PhpFile file) {
        this.file = file;
    }
}
