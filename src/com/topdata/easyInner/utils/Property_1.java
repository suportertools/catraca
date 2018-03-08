package com.topdata.easyInner.utils;

public class Property_1 {

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }
    
    public static String getAppData() {
        return System.getenv("APPDATA");
    }
}
