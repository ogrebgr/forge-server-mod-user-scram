package com.bolyartech.forge.server.module.user_scram.data.scram;

import com.bolyartech.scram_sasl.common.ScramUtils;

import java.sql.Connection;
import java.sql.SQLException;


public interface ScramDbh {
    Scram loadByUser(Connection dbc, long user) throws SQLException;

    Scram loadByUsername(Connection dbc, String username) throws SQLException;

    boolean usernameExists(Connection dbc, String username) throws SQLException;

    Scram createNew(Connection dbc, long user, String username, ScramUtils.NewPasswordStringData passwordData)
            throws SQLException;

    Scram replace(Connection dbc, long userId, String username, ScramUtils.NewPasswordStringData passwordData)
            throws SQLException;

    boolean changePassword(Connection dbc, long userId, ScramUtils.NewPasswordStringData passwordData)
            throws SQLException;

}
