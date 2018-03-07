//package com.topdata.easyInner.dao;
//
//import com.topdata.easyInner.utils.Logs;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.Properties;
//
//public class DB {
//
//    private Statement statment;
//    private String url;
//    private String port;
//    private String database;
//    private String user;
//    private String password;
//    private String driver;
//    private static Connection conn = null;
//
//    public Connection getConnection() {
//        try {
//            if (conn == null) {
//                Conf_Cliente conf_cliente = new Conf_Cliente();
//                conf_cliente.loadJson();
//                url = conf_cliente.getPostgres_ip();
//                port = conf_cliente.getPostgres_porta();
//                database = conf_cliente.getPostgres_banco();
//                user = conf_cliente.getPostgres_usuario();
//                password = conf_cliente.getPostgres_senha();
//                driver = "postgresql";
//                String uri = "jdbc:" + driver + "://" + this.url + ":" + port + "/" + database;
//                Properties props = new Properties();
//                props.setProperty("user", user);
//                props.setProperty("password", password);
//                //props.setProperty("ssl", "false");
//                props.setProperty("useSSL", "false");
//                props.setProperty("autoReconnect", "true");
//                conn = DriverManager.getConnection(uri, props);
//                return conn;
//            }
//        } catch (SQLException e) {
//            new Logs().save("database", "Problemas com a base de dados!!!! SQLException: " + e.getMessage());
//            try {
//                Thread.sleep(1000 * 60 * 5);
//            } catch (Exception e5) {
//
//            }
//        }
//        return conn;
//    }
//
//    public Statement getStatment() throws SQLException {
//        statment = getConnection().createStatement();
//        return statment;
//    }
//
//    public void setStatment(Statement statment) {
//        this.statment = statment;
//    }
//
//    public void closeStatment() throws SQLException {
//        getConnection().close();
//    }
//
//    public String getDatabase() {
//        return database;
//    }
//
//    public void setDatabase(String database) {
//        this.database = database;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public String getPort() {
//        return port;
//    }
//
//    public void setPort(String port) {
//        this.port = port;
//    }
//
//    public String getUser() {
//        return user;
//    }
//
//    public void setUser(String user) {
//        this.user = user;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public boolean isActive() {
//        try {
//            getConnection().prepareStatement("SELECT CURRENT_DATE").executeQuery();
//            return true;
//        } catch (SQLException e) {
//            return false;
//        }
//    }
//
//    public ResultSet query(String qry) {
//        try {
//            if (conn != null) {
//                ResultSet rs = conn.createStatement().executeQuery(qry);
////                getStatement().getConnection().close();
//                return rs;
//            }
//        } catch (SQLException e) {
//            e.getMessage();
//        }
//        return null;
//    }
//
//    public void query_execute(String qry) {
//        try {
//            if (conn != null) {
//                Integer x = conn.createStatement().executeUpdate(qry);
//
////                getStatement().getConnection().close();
//            }
//        } catch (SQLException e) {
//            e.getMessage();
//        }
//    }
//
//}
