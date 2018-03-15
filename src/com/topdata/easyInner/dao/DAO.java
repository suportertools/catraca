package com.topdata.easyInner.dao;

import com.topdata.easyInner.utils.Logs;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DAO {

    private static Connection conn = null;
    private Statement statement = null;
    private Boolean conectado;
    private Conf_Cliente conf_cliente;

    public Connection getConnection() {
        try {
            if (conn == null) {
                conf_cliente = new Conf_Cliente();
                conf_cliente.loadJson();
                String conexao = "jdbc:postgresql://" + conf_cliente.getPostgres_ip() + ":" + conf_cliente.getPostgres_porta() + "/" + conf_cliente.getPostgres_banco() + "?application_name=Catraca" + conf_cliente.getPostgres_cliente();
//                try {
//                    Class.forName("org.postgresql.Driver");
//                } catch (ClassNotFoundException ex) {
//                    Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
//                    System.exit(1);
//                }
                conn = DriverManager.getConnection(conexao, conf_cliente.getPostgres_usuario(), conf_cliente.getPostgres_senha());
                return conn;
            }
        } catch (SQLException e) {
            // new Logs().save("database", "Problemas com a base de dados!!!! SQLException: " + e.getMessage());
            try {
                Thread.sleep(1000 * 60 * 5);
            } catch (Exception e5) {

            }
        }
        return conn;
    }

    public ResultSet query(String qry) {
        try {
            if (getConnection() != null) {
                Connection oConnect = this.getConnection();
                PreparedStatement ps = oConnect.prepareStatement(qry);
                ResultSet rs = ps.executeQuery();
//                getStatement().getConnection().close();
                return rs;
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }

    public void query_execute(String qry) {
        try {
            if (getConnection() != null) {
                Connection oConnect = this.getConnection();
                PreparedStatement ps = oConnect.prepareStatement(qry);
                Integer x = ps.executeUpdate();
//                getStatement().getConnection().close();
            }
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    public Statement getStatement() {
//        try {
//            if (statement == null) {
//                conf_cliente = new Conf_Cliente();
//                conf_cliente.loadJson();
//                String conexao = "jdbc:postgresql://" + conf_cliente.getPostgres_ip() + ":" + conf_cliente.getPostgres_porta() + "/" + conf_cliente.getPostgres_banco();
//                Class.forName("org.postgresql.Driver");
//                Connection con = DriverManager.getConnection(conexao, conf_cliente.getPostgres_usuario(), conf_cliente.getPostgres_senha());
//
//                statement = con.createStatement();
//                conectado = true;
//            }
//        } catch (ClassNotFoundException | SQLException e) {
//            e.getMessage();
//            conectado = false;
//        }
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public Boolean getConectado() {
        return conectado;
    }

    public void setConectado(Boolean conectado) {
        this.conectado = conectado;
    }

    public boolean isActive() {
        if (getConnection() != null) {
            try {
                Connection oConnect = this.getConnection();
                PreparedStatement ps = oConnect.prepareStatement("SELECT CURRENT_DATE");
                ps.executeQuery();
                return true;
            } catch (SQLException e) {
                System.err.println("Erro de conexão: " + e.getMessage());
                if (e.getMessage().toLowerCase().contains("connection has been closed")) {
                    try {
                        Thread.sleep(1000 * 60 * 1);
                        conn = null;
                        getConnection();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                new Logs().save("Conexão", e.getMessage());
                return false;
            }
        }
        return false;
    }
}
