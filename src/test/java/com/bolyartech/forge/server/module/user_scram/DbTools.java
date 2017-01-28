package com.bolyartech.forge.server.module.user_scram;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DbTools {
    public static void deleteAllUsers(Connection dbc) throws SQLException {
        String sql = "DELETE FROM users";
        try (PreparedStatement psDelete = dbc.prepareStatement(sql)) {
            psDelete.executeUpdate();
        }
    }


    public static void deleteAllScrams(Connection dbc) throws SQLException {
        String sql = "DELETE FROM user_scram";
        try (PreparedStatement psDelete = dbc.prepareStatement(sql)) {
            psDelete.executeUpdate();
        }
    }


    public static void deleteAllScreenNames(Connection dbc) throws SQLException {
        String sql = "DELETE FROM user_screen_names";
        try (PreparedStatement psDelete = dbc.prepareStatement(sql)) {
            psDelete.executeUpdate();
        }
    }
}
