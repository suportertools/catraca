package com.topdata.easyInner.ws;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.topdata.easyInner.utils.JSONS;
import javax.ws.rs.core.MultivaluedMap;

public class FunctionsWS {

    public static Integer catraca(Integer nr_pessoa, Integer departamento_id, Integer tipo, Integer via) {

        Integer result = null;
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("nr_pessoa", nr_pessoa + "");
        form.add("departamento_id", departamento_id + "");
        form.add("tipo", tipo + "");
        form.add("via", via + "");

        try {

            Object r = JSONS.post("functions/catraca", form);
            if(r != null) {
                result = Integer.parseInt((String) r);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

}
