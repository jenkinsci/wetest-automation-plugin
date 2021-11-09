package com.tencent.wetest.plugin.util;

import hudson.FilePath;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class FileUtils {

    public static String getAbsPath(String path) throws IOException, InterruptedException {
        if (TextUtils.isBlank(path)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.trim(path);
    }

    public static boolean isExist(String path) {
        return new File(path).exists();
    }
}
