package com.example.takeiteasy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLConn
{
    private static final String MYSQL_HOST = "192.168.43.209:3306";
    private static final String DB_NAME = "takeiteasy";
    private static final String DB_USER = "manager";
    private static final String DB_PASSWORD = "Wheelchair";
    private static Connection conn = null;

    private static void connect() throws ClassNotFoundException, SQLException
    {
        String url = String.format("jdbc:mysql://%s/%s?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8", MYSQL_HOST, DB_NAME);
        Class.forName("com.mysql.jdbc.Driver");

        if (conn == null || !conn.isValid(10))
            conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }

    public static ResultSet fetch(String sql, Object... values) throws ClassNotFoundException, SQLException
    {
        connect();
        PreparedStatement ps = conn.prepareStatement(sql);

        for (int i = 0; i < values.length; i++)
            ps.setString(i + 1, String.valueOf(values[i]));

        return ps.executeQuery();
    }

    public static int alternate(String sql, Object... values) throws ClassNotFoundException, SQLException
    {
        connect();
        PreparedStatement ps = conn.prepareStatement(sql);

        for (int i = 0; i < values.length; i++)
            ps.setString(i + 1, String.valueOf(values[i]));

        return ps.executeUpdate();
    }

    public static void close(Connection conn) throws SQLException
    {
        conn.close();
    }
}