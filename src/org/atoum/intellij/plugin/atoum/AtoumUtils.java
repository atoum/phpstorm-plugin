package org.atoum.intellij.plugin.atoum;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.lang.psi.PhpFile;
import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.Nullable;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AtoumUtils {

    public static VirtualFile findTestBaseDir(VirtualFile currentDir, Project project)
    {
        Boolean continueSearch = true;
        Integer maxDirs = 35;
        Integer dirCount = 0;
        while (continueSearch) {
            dirCount++;
            if (currentDir.equals(project.getBaseDir())) {
                continueSearch = false;
            } else if (dirCount >= maxDirs) {
                continueSearch = false;
            } else {
                if (new File(currentDir.getPath() + "/composer.json").exists()) {
                    return currentDir;
                }
            }
            currentDir = currentDir.getParent();
            if (null == currentDir) {
                return project.getBaseDir();
            }
        }

        return project.getBaseDir();
    }

    public static VirtualFile findTestBaseDir(PhpFile phpFile, Project project)
    {
        return findTestBaseDir(phpFile.getContainingDirectory().getVirtualFile(), project);
    }

    public static VirtualFile findTestBaseDir(RunnerConfiguration runnerConfiguration, Project project) throws Exception {
        if (null != runnerConfiguration.getFile()) {
            return findTestBaseDir(runnerConfiguration.getFile(), project);
        }

        if (null != runnerConfiguration.getDirectory()) {
            return findTestBaseDir(runnerConfiguration.getDirectory(), project);
        }

        throw new Exception("Bad configuration : no file or directory defined");
    }

    public static String findAtoumBinPath(VirtualFile dir)
    {
        String defaultBinPath = dir.getPath() + "/vendor/bin/atoum";
        String atoumBinPath = defaultBinPath;

        String binDir = getComposerBinDir(dir.getPath() + "/composer.json");
        String binPath = dir.getPath() + "/" + binDir + "/atoum";
        if (null != binDir && new File(binPath).exists()) {
            atoumBinPath = binPath;
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            atoumBinPath += ".bat";
        }

        return atoumBinPath;
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
