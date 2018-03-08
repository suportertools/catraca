package com.topdata.easyInner.ws;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.topdata.easyInner.settings.WebService;
import com.topdata.easyInner.utils.JSONS;
import java.util.Date;
import javax.ws.rs.core.MultivaluedMap;

public class AuthWS {

    public static WebService WEBSERVICE = null;
    public static String TOKEN = null;
    public static Date EXPIRES = null;

    public AuthWS() {
        if (WEBSERVICE == null) {
            WebService webService = new WebService();
            webService.loadJson();
            WEBSERVICE = webService;
        }
    }

    public static void logout() {
        JSONS.get("auth/logout");
    }

    public static void login() {

        String token = (String) JSONS.get("auth/login");

        if (token == null || token.isEmpty()) {
            System.err.println("");
            System.exit(1);
            return;
        }
        TOKEN = token;
    }

    public static void clear1(Integer catraca_id) {
        MultivaluedMap form = new MultivaluedMapImpl();
        form.add("catraca_id", catraca_id);
        JSONS.post("/catraca_monitora/clear1", form);
    }

    public static void clear2(Integer catraca_id, String json) {
        JSONS.get("/catraca_monitora/clear2/" + catraca_id + "/" + json);
    }

}
