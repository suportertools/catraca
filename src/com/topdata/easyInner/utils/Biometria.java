package com.topdata.easyInner.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class Biometria {

    public static Map<String, Integer> RECEBIDA = new LinkedHashMap<String, Integer>();

    public static Integer getInner(String comparer) {
        if (RECEBIDA != null && !RECEBIDA.isEmpty()) {
            for (Map.Entry<String, Integer> entry : RECEBIDA.entrySet()) {
                if (entry.getKey().equals(comparer)) {
                    Integer value = entry.getValue();
                    RECEBIDA.remove(comparer);
                    return value;
                }
            }
        }
        return -1;
    }

    public static Integer getIp(String comparer) {
        if (RECEBIDA != null && !RECEBIDA.isEmpty()) {
            for (Map.Entry<String, Integer> entry : RECEBIDA.entrySet()) {
                if (entry.getKey().equals(comparer)) {
                    Integer value = entry.getValue();
                    Debugs.breakPoint("getIP: " + value);
                    RECEBIDA.remove(comparer);
                    return value;
                }
            }
        }
        return -1;
    }

}
