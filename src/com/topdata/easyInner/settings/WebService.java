package com.topdata.easyInner.settings;

import com.topdata.easyInner.utils.Path;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class WebService {

    private String host = "localhost";
    private Integer port = 8080;
    private String context = "perifericosws";
    private String client = "Sindical";
    private Boolean ssl = false;
    private Boolean token = true;
    private String mac = "";
    private Integer device_type = null;

    public void loadJson() {
        String path = Path.getRealPath();
        try {
            File file = new File(path + File.separator + "conf" + File.separator + "web_service.json");
            if (!file.exists()) {
                return;
            }
            String json = null;
            try {
                json = new String(Files.readAllBytes(Paths.get(file.getPath())));
            } catch (IOException ex) {
                Logger.getLogger(WebService.class.getName()).log(Level.SEVERE, null, ex);
            }
            JSONObject jSONObject = new JSONObject(json);

            try {
                this.host = jSONObject.getString("host");
            } catch (JSONException e) {
            }
            try {
                this.port = jSONObject.getInt("port");
            } catch (JSONException e) {
            }
            try {
                this.context = jSONObject.getString("context");
            } catch (JSONException e) {
            }
            try {
                this.client = jSONObject.getString("client");
            } catch (JSONException e) {
            }
            try {
                this.ssl = jSONObject.getBoolean("ssl");
            } catch (JSONException e) {
            }
            try {
                this.token = jSONObject.getBoolean("token");
            } catch (JSONException e) {
            }
            try {
                this.mac = jSONObject.getString("mac");
            } catch (JSONException e) {

            }
            try {
                this.device_type = jSONObject.getInt("device_type");
            } catch (JSONException e) {

            }
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public Boolean getToken() {
        return token;
    }

    public void setToken(Boolean token) {
        this.token = token;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getUri() {
        String uri = "http://";
        if (ssl) {
            uri = "https://";
        }
        if (!host.isEmpty()) {
            uri += host;
        }
        if (port != 80) {
            uri += ":" + port;
        }
        if (!context.isEmpty()) {
            uri += "/" + context;
        }
        uri += "/ws/";
        return uri;
    }

    public Integer getDevice_type() {
        return device_type;
    }

    public void setDevice_type(Integer device_type) {
        this.device_type = device_type;
    }

}
