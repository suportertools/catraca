package com.topdata.easyInner.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DAO {

    private Statement statement;
    private Boolean conectado;
    private final Conf_Cliente conf_cliente = new Conf_Cliente();

    public DAO() {
        try {
            conf_cliente.loadJson();

            String conexao = "jdbc:postgresql://" + conf_cliente.getPostgres_ip() + ":" + conf_cliente.getPostgres_porta() + "/" + conf_cliente.getPostgres_banco();
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(conexao, conf_cliente.getPostgres_usuario(), conf_cliente.getPostgres_senha());

            statement = con.createStatement();
            conectado = true;
        } catch (ClassNotFoundException | SQLException e) {
            e.getMessage();
            conectado = false;
        }
    }

//    public void disconect() {
//        try {
//            getStatement().close();
//        } catch (SQLException ex) {
//            ex.getMessage();
//        }
//    }
    public ResultSet query(String qry) {
        try {
            if (getStatement() != null) {
                ResultSet rs = getStatement().executeQuery(qry);
                getStatement().getConnection().close();
                return rs;
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }

    public void query_execute(String qry) {
        try {
            if (getStatement() != null) {
                Integer x = getStatement().executeUpdate(qry);
                getStatement().getConnection().close();
            }
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    public Statement getStatement() {
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
}
