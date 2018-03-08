package com.topdata.easyInner.ws;

import com.sun.jersey.api.client.ClientResponse;
import com.topdata.easyInner.utils.JSONS;

public class StatusWS {

    public static Boolean isAtive() {

        ClientResponse response = JSONS.getClientResponse("status");

        if (response.getStatus() == 200) {
            return true;
        }
        return false;
    }

}
