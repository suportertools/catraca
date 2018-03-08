package com.topdata.easyInner.ws;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.topdata.easyInner.utils.JSONS;
import com.topdata.easyInner.utils.RetornoJson;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

public class PessoaWS {

    public static RetornoJson select(Integer pessoa_id) {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        RetornoJson retornoJson = null;
        try {
            MultivaluedMap form = new MultivaluedMapImpl();
            form.add("pessoa_id", pessoa_id + "");
            ClientResponse response = JSONS.postClientResponse("pessoa/select", form, MediaType.APPLICATION_FORM_URLENCODED, "application/json", config);
            
            if(response == null) {
                return new RetornoJson();                
            }
            
            if (response.getStatus() != 200) {
                return retornoJson;
            }

            retornoJson = response.getEntity(new GenericType<RetornoJson>() {
            });
            if (retornoJson != null) {
                return retornoJson;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return retornoJson;
    }

}
