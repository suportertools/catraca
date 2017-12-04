package com.topdata.easyInner.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DAO {

    private Statement statement;
    private Boolean conectado;
    private final Conf_t conf_t = new Conf_t();

    public DAO() {
        try {
            conf_t.loadJson();

            String conexao = "jdbc:postgresql://" + conf_t.getPostgres_ip() + ":5432/" + conf_t.getPostgres_banco();
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(conexao, conf_t.getPostgres_usuario(), conf_t.getPostgres_senha());

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
            if (getStatement() != null){
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
            if (getStatement() != null){
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
