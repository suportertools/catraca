package com.topdata.easyInner.ws;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.topdata.easyInner.dao.Catraca;
import com.topdata.easyInner.utils.JSONS;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

public class CatracaWS {

    public List<Catraca> listaCatraca() {

        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        List<Catraca> list = new ArrayList();
        try {
            ClientResponse response = JSONS.postClientResponse("catraca/load", null, MediaType.APPLICATION_JSON, "application/json", config);

            if (response.getStatus() != 200) {
                return list;
            }

            list = response.getEntity(new GenericType<List<Catraca>>() {
            });
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return list;
    }

    public final Catraca pesquisaCatraca(Integer id_catraca) {
        return null;
    }
}
