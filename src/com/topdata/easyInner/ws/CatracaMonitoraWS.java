package com.topdata.easyInner.ws;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.topdata.easyInner.utils.JSONS;
import com.topdata.easyInner.utils.RetornoJson;
import javax.ws.rs.core.MultivaluedMap;

public class CatracaMonitoraWS {

    public static void start() {
        JSONS.post("/catraca_monitora/start", "");
    }

    public static void delete(Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("catraca_id", catraca_id + "");
        JSONS.post("/catraca_monitora/delete", form);
    }

    public static void store(Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("catraca_id", catraca_id + "");
        JSONS.post("/catraca_monitora/store", form);
    }

    public static void status(Integer random_id, Boolean ativo, String status, Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("random_id", random_id + "");
        form.add("ativo", ativo + "");
        form.add("status", status + "");
        form.add("catraca_id", catraca_id + "");
        JSONS.post("/catraca_monitora/status", form);
    }

    public static void ping(Integer random_id, Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("random_id", random_id + "");
        form.add("catraca_id", catraca_id + "");
        JSONS.post("/catraca_monitora/ping", form);
    }

    public static Integer is_atualizar(Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("catraca_id", catraca_id + "");
        Integer result = null;
        try {
            Object o = JSONS.post("/catraca_monitora/atualizar", form);
            if (o != null) {
                result = Integer.parseInt((String) o);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    public static void clear1(Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("catraca_id", catraca_id + "");
        JSONS.post("/catraca_monitora/clear1", form);
    }

    public static void clear2(Integer catraca_id, RetornoJson json) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("nr_pessoa", json.getNr_pessoa() + "");
        form.add("nome", json.getNome()+ "");
        form.add("foto", json.getFoto()+ "");
        form.add("observacao", json.getObservacao() + "");
        form.add("mensagem", json.getMensagem());
        form.add("liberado", json.getLiberado() + "");
        form.add("catraca_id", catraca_id + "");
        JSONS.post("/catraca_monitora/clear2", form);
    }

}
