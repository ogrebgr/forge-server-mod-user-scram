package com.bolyartech.forge.server.module.user_scram.data.scram;

import com.bolyartech.scram_sasl.common.ScramUtils;
import com.google.common.base.Strings;

import java.sql.*;


public class ScramDbhImpl implements ScramDbh {
    private static final String USERS_TABLE_NAME = "user_scram";

    private final String mTableName;


    public ScramDbhImpl() {
        mTableName = getTableName();
    }


    @Override
    public Scram loadByUser(Connection dbc, long user) throws SQLException {
        if (user <= 0) {
            throw new IllegalStateException("user <= 0");
        }

        String sql = "SELECT username, salt, server_key, stored_key, iterations " +
                "FROM " + mTableName +
                " WHERE user = ?";
        try (PreparedStatement psLoad = dbc.prepareStatement(sql)) {
            psLoad.setLong(1, user);

            try (ResultSet rs = psLoad.executeQuery()) {
                if (rs.next()) {
                    return new Scram(user,
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getInt(5)
                    );
                } else {
                    return null;
                }
            }

        }
    }


    @Override
    public Scram loadByUsername(Connection dbc, String username) throws SQLException {
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalStateException("username empty");
        }

        String sql = "SELECT user, salt, server_key, stored_key, iterations " +
                "FROM " + mTableName +
                " WHERE username = ?";
        try (PreparedStatement psLoad = dbc.prepareStatement(sql)) {
            psLoad.setString(1, username);

            try (ResultSet rs = psLoad.executeQuery()) {
                if (rs.next()) {
                    return new Scram(rs.getLong(1),
                            username,
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getInt(5)
                    );
                } else {
                    return null;
                }
            }

        }
    }


    @Override
    public boolean usernameExists(Connection dbc, String username) throws SQLException {
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalStateException("username empty");
        }

        String sql = "SELECT user " +
                "FROM " + mTableName +
                " WHERE username_lc = ?";
        try (PreparedStatement psLoad = dbc.prepareStatement(sql)) {
            psLoad.setString(1, username.toLowerCase());

            try (ResultSet rs = psLoad.executeQuery()) {
                return rs.next();
            }
        }
    }


    @Override
    public Scram createNew(Connection dbc, long user, String username, ScramUtils.NewPasswordStringData passwordData)
            throws SQLException {

        try {
            String sqlLock = "LOCK TABLES " + mTableName + " WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (!usernameExists(dbc, username)) {
                Scram ret = new Scram(user, username, passwordData.salt, passwordData.serverKey,
                        passwordData.storedKey, passwordData.iterations);

                String sql = "INSERT INTO " + mTableName + " " +
                        "(user, username, salt, server_key, stored_key, iterations, username_lc) " +
                        "VALUES (?,?,?,?,?,?,?)";

                try (PreparedStatement psInsert = dbc.prepareStatement(sql)) {
                    psInsert.setLong(1, user);
                    psInsert.setString(2, username);
                    psInsert.setString(3, passwordData.salt);
                    psInsert.setString(4, passwordData.serverKey);
                    psInsert.setString(5, passwordData.storedKey);
                    psInsert.setInt(6, passwordData.iterations);
                    psInsert.setString(7, username.toLowerCase());
                    psInsert.executeUpdate();
                }

                return ret;
            } else {
                return null;
            }
        } finally {
            String sqlLock = "UNLOCK TABLES";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);
        }
    }


    @Override
    public Scram replace(Connection dbc, long userId, String username, ScramUtils.NewPasswordStringData passwordData)
            throws SQLException {

        Scram ret = new Scram(userId, username, passwordData.salt, passwordData.serverKey,
                passwordData.storedKey, passwordData.iterations);

        String sql = "UPDATE " + mTableName + " SET " +
                "username = ?, " +
                "salt = ?," +
                "server_key = ?," +
                "stored_key = ?," +
                "iterations = ?, " +
                "username_lc = ? " +
                "WHERE user = ?";
        try (PreparedStatement psUpdate = dbc.prepareStatement(sql)) {
            psUpdate.setString(1, username);
            psUpdate.setString(2, passwordData.salt);
            psUpdate.setString(3, passwordData.serverKey);
            psUpdate.setString(4, passwordData.storedKey);
            psUpdate.setInt(5, passwordData.iterations);
            psUpdate.setString(6, username.toLowerCase());
            psUpdate.setLong(7, userId);
            psUpdate.executeUpdate();
        }

        return ret;
    }


    @Override
    public boolean changePassword(Connection dbc, long userId, ScramUtils.NewPasswordStringData passwordData)
            throws SQLException {

        String sql = "UPDATE " + mTableName + " SET " +
                "salt = ?," +
                "server_key = ?," +
                "stored_key = ?," +
                "iterations = ? " +
                "WHERE user = ?";

        try (PreparedStatement psUpdate = dbc.prepareStatement(sql)) {
            psUpdate.setString(1, passwordData.salt);
            psUpdate.setString(2, passwordData.serverKey);
            psUpdate.setString(3, passwordData.storedKey);
            psUpdate.setInt(4, passwordData.iterations);
            psUpdate.setLong(5, userId);
            int count = psUpdate.executeUpdate();

            return count == 1;
        }
    }


    protected String getTableName() {
        return USERS_TABLE_NAME;
    }
}
