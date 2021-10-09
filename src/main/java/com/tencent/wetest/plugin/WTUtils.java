package com.tencent.wetest.plugin;

import hudson.FilePath;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

class WTUtils {

    static String getAbsPath(FilePath workspace, String path) throws IOException, InterruptedException {
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
}
