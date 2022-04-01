package com.tencent.wetest.plugin.util;

import hudson.FilePath;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class FileUtils {

    public static String getAbsPath(String path) throws IOException, InterruptedException {
        if (path.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return StringUtils.trim(path);
    }

    public static boolean isExist(String path) {
        return new File(path).exists();
    }
}
