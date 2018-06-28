package com.jesus_crie.modularbot;

public class ModularBotBuildInfo {

    public static final String AUTHOR = "Jesus_Crie";
    public static final String GITHUB_URL = "https://github.com/JesusCrie/ModularBot_v2";
    public static final String VERSION_NAME = "@versionName@";
    private static final String BUILD_NUMBER = "@buildNumber@";

    public static int BUILD_NUMBER() {
        try {
            return Integer.parseInt(BUILD_NUMBER);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
