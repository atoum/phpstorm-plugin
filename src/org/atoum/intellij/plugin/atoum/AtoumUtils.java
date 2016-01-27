package org.atoum.intellij.plugin.atoum;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.lang.psi.PhpFile;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AtoumUtils {

    public static VirtualFile findTestBaseDir(PhpFile phpFile, Project project)
    {
        Boolean continueSearch = true;
        Integer maxDirs = 35;
        Integer dirCount = 0;
        PsiDirectory currentDir = phpFile.getContainingDirectory();
        while (continueSearch) {
            dirCount++;
            if (currentDir.getVirtualFile().equals(project.getBaseDir())) {
                continueSearch = false;
            } else if (dirCount >= maxDirs) {
                continueSearch = false;
            } else {
                if (new File(currentDir.getVirtualFile().getPath() + "/composer.json").exists()) {
                    return currentDir.getVirtualFile();
                }
            }
            currentDir = currentDir.getParentDirectory();
            if (null == currentDir) {
                return project.getBaseDir();
            }
        }

        return project.getBaseDir();
    }

    public static String findAtoumBinPath(VirtualFile dir)
    {
        String defaultBinPath = dir.getPath() + "/vendor/bin/atoum";

        String binDir = getComposerBinDir(dir.getPath() + "/composer.json");
        String binPath = dir.getPath() + "/" + binDir + "/atoum";
        if (null != binDir && new File(binPath).exists()) {
            return binPath;
        }

        return defaultBinPath;
    }

    @Nullable
    protected static String getComposerBinDir(String composerPath) {
        try {
            String composerJsonContent = new String(Files.readAllBytes(Paths.get(composerPath)));
            JSONObject obj = new JSONObject(composerJsonContent);
            return obj.getJSONObject("config").get("bin-dir").toString();
        } catch (JSONException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

}
