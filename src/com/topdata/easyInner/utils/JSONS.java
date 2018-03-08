package com.topdata.easyInner.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.topdata.easyInner.ws.AuthWS;
import static com.topdata.easyInner.ws.AuthWS.WEBSERVICE;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class JSONS {

    public static Object get(String path) {
        Object output = null;
        path = path.replace("//", "/");
        try {
            if (path.substring(0, 1).contains("/")) {
                path = path.substring(1);
            }
        } catch (Exception e) {

        }
        String host = WEBSERVICE.getUri() + path;
        try {

            Client client = Client.create();

            WebResource webResource = client.resource(host);

            String token;
            if (AuthWS.TOKEN == null) {
                token = Mac.getInstance();
            } else {
                token = AuthWS.TOKEN;
            }

            ClientResponse response = webResource
                    .accept("application/json")
                    .header("Authorization", token)
                    .header("Device-Type", WEBSERVICE.getDevice_type())
                    .header("Client", WEBSERVICE.getClient())
                    .header("Device-Mac", Mac.getInstance())
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                return null;
            }

            output = response.getEntity(String.class);

            System.out.println("Output from Server .... \n");
            System.out.println(output);

        } catch (Exception e) {

            e.printStackTrace();

        }
        return output;
    }

    public static ClientResponse getClientResponse(String path) {
        ClientResponse response = null;
        Object output = null;
        path = path.replace("//", "/");
        try {
            if (path.substring(0, 1).contains("/")) {
                path = path.substring(1);
            }
        } catch (Exception e) {

        }
        String host = WEBSERVICE.getUri() + path;
        try {

            Client client = Client.create();

            WebResource webResource = client.resource(host);

            String token;
            if (AuthWS.TOKEN == null) {
                token = Mac.getInstance();
            } else {
                token = AuthWS.TOKEN;
            }

            response = webResource
                    .accept("application/json")
                    .header("Authorization", token)
                    .header("Device-Type", WEBSERVICE.getDevice_type())
                    .header("Client", WEBSERVICE.getClient())
                    .header("Device-Mac", Mac.getInstance()).
                    get(ClientResponse.class);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return response;
    }

    public static Object post(String path, String json) {
        Object output = null;
        path = path.replace("//", "/");
        try {
            if (path.substring(0, 1).contains("/")) {
                path = path.substring(1);
            }
        } catch (Exception e) {

        }
        String host = WEBSERVICE.getUri() + path;
        try {

            Client client = Client.create();

            String token;
            if (AuthWS.TOKEN == null) {
                token = Mac.getInstance();
            } else {
                token = AuthWS.TOKEN;
            }
            ClientResponse response;
            if (json == null || json.isEmpty()) {
                response
                        = client.resource(host)
                        .header("Authorization", token)
                        .header("Device-Type", WEBSERVICE.getDevice_type())
                        .header("Device-Mac", Mac.getInstance())
                        .header("Client", WEBSERVICE.getClient())
                        .post(ClientResponse.class);
            } else {
                response
                        = client.resource(host)
                        .header("Authorization", token)
                        .header("Device-Type", WEBSERVICE.getDevice_type())
                        .header("Client", WEBSERVICE.getClient())
                        .header("Device-Mac", Mac.getInstance())
                        .post(ClientResponse.class, Entity.entity(json, MediaType.APPLICATION_JSON));
            }

            if (response.getStatus() != 200) {
                return null;
            }

            System.out.println("Output from Server .... \n");
            output = response.getEntity(String.class);
            System.out.println(output);

        } catch (Exception e) {

            e.printStackTrace();

        }
        return output;
    }

    public static Object post(String path, MultivaluedMap form) {
        Object output = null;
        path = path.replace("//", "/");
        try {
            if (path.substring(0, 1).contains("/")) {
                path = path.substring(1);
            }
        } catch (Exception e) {

        }
        String host = WEBSERVICE.getUri() + path;
        try {

            String input = "{\"singer\":\"Metallica\",\"title\":\"Fade To Black\"}";

            Client client = Client.create();

            String token;
            if (AuthWS.TOKEN == null) {
                token = Mac.getInstance();
            } else {
                token = AuthWS.TOKEN;
            }
            ClientResponse response
                    = client.resource(host)
                    .header("Authorization", token)
                    .header("Device-Type", WEBSERVICE.getDevice_type())
                    .header("Client", WEBSERVICE.getClient())
                    .header("Device-Mac", Mac.getInstance())
                    .type(MediaType.APPLICATION_FORM_URLENCODED)
                    .post(ClientResponse.class, form);

            if (response.getStatus() != 200) {
                return null;
            }

            System.out.println("Output from Server .... \n");
            output = response.getEntity(String.class);
            System.out.println(output);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static ClientResponse postClientResponse(String path, MultivaluedMap form) {
        return postClientResponse(path, form, null, null, null);
    }

    public static ClientResponse postClientResponse(String path, MultivaluedMap form, String mediaType, String accept, ClientConfig config) {
        ClientResponse response = null;
        path = path.replace("//", "/");
        try {
            if (path.substring(0, 1).contains("/")) {
                path = path.substring(1);
            }
        } catch (Exception e) {

        }
        String host = WEBSERVICE.getUri() + path;
        try {

            Client client;

            if (config == null) {
                client = Client.create();
            } else {
                client = Client.create(config);
            }

            String token;
            if (AuthWS.TOKEN == null) {
                token = Mac.getInstance();
            } else {
                token = AuthWS.TOKEN;
            }
            String mt = "";
            if (mediaType == null) {
                mt = MediaType.APPLICATION_FORM_URLENCODED;
            }
            if (accept == null) {
                if (form == null) {
                    response
                            = client.resource(host)
                            .header("Authorization", token)
                            .header("Device-Type", WEBSERVICE.getDevice_type())
                            .header("Client", WEBSERVICE.getClient())
                            .header("Device-Mac", Mac.getInstance())
                            .type(mt)
                            .post(ClientResponse.class);

                } else {
                    response
                            = client.resource(host)
                            .header("Authorization", token)
                            .header("Device-Type", WEBSERVICE.getDevice_type())
                            .header("Client", WEBSERVICE.getClient())
                            .header("Device-Mac", Mac.getInstance())
                            .type(mt)
                            .post(ClientResponse.class, form);
                }
            } else if (form == null) {
                response
                        = client.resource(host)
                        .header("Authorization", token)
                        .header("Device-Type", WEBSERVICE.getDevice_type())
                        .header("Client", WEBSERVICE.getClient())
                        .header("Device-Mac", Mac.getInstance())
                        .accept(accept)
                        .post(ClientResponse.class);

            } else {
                response
                        = client.resource(host)
                        .header("Authorization", token)
                        .header("Device-Type", WEBSERVICE.getDevice_type())
                        .header("Client", WEBSERVICE.getClient())
                        .header("Device-Mac", Mac.getInstance())
                        .accept(accept)
                        //.accept(accept)
                        .post(ClientResponse.class, form);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static Boolean isActive() {
        String host = WEBSERVICE.getUri() + "status";
        Object output = null;
        try {

            Client client = Client.create();

            String token;
            if (AuthWS.TOKEN == null) {
                token = Mac.getInstance();
            } else {
                token = AuthWS.TOKEN;
            }

            ClientResponse response
                    = client.resource(host)
                    .header("Authorization", token)
                    .header("Device-Type", WEBSERVICE.getDevice_type())
                    .header("Client", WEBSERVICE.getClient())
                    .header("Device-Mac", Mac.getInstance())
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                return false;
            }

            String output2 = response.getEntity(String.class);
            if (output2.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;

    }

}
