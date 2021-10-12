package com.tencent.wetest.plugin.util;

import hudson.FilePath;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class FileUtils {

    public static String getAbsPath(FilePath workspace, String path) throws IOException, InterruptedException {
        if (TextUtils.isBlank(path)) {
            return StringUtils.EMPTY;
        }
        String trimmed = StringUtils.trim(path);
        if (trimmed.startsWith(File.separator)) {
            return trimmed;
        } else {
            URI workspaceURI = workspace.toURI();
            return workspaceURI.getPath() + trimmed;
        }
    }

    public static boolean isExist(String path) {
        return new File(path).exists();
    }
}
