package com.linhei.queryuserid.utils;


public class Util {
    public static void LogFileWrite(String path, String content) {

        FileUtils.fileLinesWrite(path,
                content, true);
    }
}
