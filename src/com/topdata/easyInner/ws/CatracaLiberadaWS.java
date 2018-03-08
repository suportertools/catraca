package com.topdata.easyInner.ws;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.topdata.easyInner.utils.JSONS;
import javax.ws.rs.core.MultivaluedMap;

public class CatracaLiberadaWS {

    public static void liberar(Integer inner, Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("inner", inner + "");
        form.add("catraca_id", catraca_id + "");
        JSONS.post("/catraca_liberada/liberar", form);
    }

}
