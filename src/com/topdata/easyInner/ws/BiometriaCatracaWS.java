package com.topdata.easyInner.ws;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.topdata.easyInner.utils.JSONS;
import javax.ws.rs.core.MultivaluedMap;

public class BiometriaCatracaWS {

    public static Integer exists(String ip) {

        Integer result = null;
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("ip", ip);
        try {

            Object r = JSONS.post("biometria_catraca/exists", form);
            if (r != null) {
                result = Integer.parseInt((String) r);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

}
