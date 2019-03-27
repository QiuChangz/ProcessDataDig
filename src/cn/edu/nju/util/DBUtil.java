package cn.edu.nju.util;

import java.sql.*;

public class DBUtil {

    public static Connection getMySqlDBConnection(String url){
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + url + "?useSSL=false&useUnicode=true&characterEncoding=UTF-8",
                    PropertiesUtil.getProperties("user"),
                    PropertiesUtil.getProperties("password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
    public static Connection getSqliteDBConnection(String dbFilePath){
        Connection con = null;

        try{
            Class.forName("org.sqlite.JDBC");

            con = DriverManager.getConnection("jdbc:sqlite:"+dbFilePath);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return con;
    }

    public static void elegantlyClose(Connection connection, Statement statement, ResultSet resultSet){
        try {
            if(resultSet != null && !resultSet.isClosed()){
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if(statement != null && !statement.isClosed()){
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if(connection != null && !connection.isClosed()){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
