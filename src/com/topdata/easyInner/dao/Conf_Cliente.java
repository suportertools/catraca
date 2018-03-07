package com.topdata.easyInner.dao;

import com.topdata.easyInner.utils.Path;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Conf_Cliente {

    public void loadJson() {
        String path = Path.getRealPath();
        try {
            File file = new File(path + File.separator + "conf" + File.separator + "configuracao.json");
            if (!file.exists()) {
                return;
            }
            String json = null;
            try {
                json = new String(Files.readAllBytes(Paths.get(file.getPath())));
            } catch (IOException ex) {
                Logger.getLogger(Conf_Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }

            JSONObject jSONObject = new JSONObject(json);

            this.postgres_ip = jSONObject.getString("postgres_ip");
            this.postgres_usuario = jSONObject.getString("postgres_usuario");
            this.postgres_senha = jSONObject.getString("postgres_senha");
            this.postgres_banco = jSONObject.getString("postgres_banco");
            this.postgres_cliente = jSONObject.getString("postgres_cliente");
            this.servidor_monitor = jSONObject.getString("servidor_monitor");

            try {
                this.postgres_porta = jSONObject.getString("postgres_porta");
            } catch (JSONException e) {
                this.postgres_porta = "5432";
            }

            try {
                this.web_service = jSONObject.getBoolean("web_service");
            } catch (JSONException e) {
                this.web_service = false;
            }

//            if (this.postgres_senha.isEmpty()) {
//                if (postgres_banco.equals("Sindical")) {
//                    this.postgres_senha = "989899";
//                } else {
//                    this.postgres_senha = "r#@tools";
//                }
//            }
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String postgres_ip;
    private String postgres_usuario;
    private String postgres_senha;
    private String postgres_banco;
    private String postgres_cliente;
    private String postgres_porta;
    private String servidor_monitor;
    private Boolean web_service;

    public Conf_Cliente() {
        this.postgres_ip = "";
        this.postgres_usuario = "";
        this.postgres_senha = "";
        this.postgres_banco = "";
        this.postgres_cliente = "";
        this.postgres_porta = "5432";
        this.servidor_monitor = "";
    }

    public Conf_Cliente(String postgres_ip, String postgres_usuario, String postgres_senha, String postgres_banco, String postgres_cliente, String postgres_porta, String servidor_monitor, Boolean web_service) {
        this.postgres_ip = postgres_ip;
        this.postgres_usuario = postgres_usuario;
        this.postgres_senha = postgres_senha;
        this.postgres_banco = postgres_banco;
        this.postgres_cliente = postgres_cliente;
        this.postgres_porta = postgres_porta;
        this.servidor_monitor = servidor_monitor;
        this.web_service = web_service;
    }

    public String getPostgres_ip() {
        return postgres_ip;
    }

    public void setPostgres_ip(String postgres_ip) {
        this.postgres_ip = postgres_ip;
    }

    public String getPostgres_usuario() {
        return postgres_usuario;
    }

    public void setPostgres_usuario(String postgres_usuario) {
        this.postgres_usuario = postgres_usuario;
    }

    public String getPostgres_senha() {
        return postgres_senha;
    }

    public void setPostgres_senha(String postgres_senha) {
        this.postgres_senha = postgres_senha;
    }

    public String getPostgres_banco() {
        return postgres_banco;
    }

    public void setPostgres_banco(String postgres_banco) {
        this.postgres_banco = postgres_banco;
    }

    public String getPostgres_cliente() {
        return postgres_cliente;
    }

    public void setPostgres_cliente(String postgres_cliente) {
        this.postgres_cliente = postgres_cliente;
    }

    public String getServidor_monitor() {
        return servidor_monitor;
    }

    public void setServidor_monitor(String servidor_monitor) {
        this.servidor_monitor = servidor_monitor;
    }

    /**
     * @return the postgres_porta
     */
    public String getPostgres_porta() {
        return postgres_porta;
    }

    /**
     * @param postgres_porta the postgres_porta to set
     */
    public void setPostgres_porta(String postgres_porta) {
        this.postgres_porta = postgres_porta;
    }

    public Boolean getWeb_service() {
        return web_service;
    }

    public void setWeb_service(Boolean web_service) {
        this.web_service = web_service;
    }

}
