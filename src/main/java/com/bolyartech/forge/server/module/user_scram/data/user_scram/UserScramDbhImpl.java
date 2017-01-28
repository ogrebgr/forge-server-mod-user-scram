package com.bolyartech.forge.server.module.user_scram.data.user_scram;

import com.bolyartech.forge.server.module.user.data.UserLoginType;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.Scram;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.scram_sasl.common.ScramUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class UserScramDbhImpl implements UserScramDbh {

    @Override
    public UserScram createNew(Connection dbc,
                               UserDbh userDbh,
                               ScramDbh scramDbh,
                               String username,
                               ScramUtils.NewPasswordStringData data) throws SQLException {

        try {
            String sqlLock = "LOCK TABLES users WRITE, user_scram WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (!scramDbh.usernameExists(dbc, username)) {
                try {
                    dbc.setAutoCommit(false);
                    User user = userDbh.createNew(dbc, false, UserLoginType.SCRAM);
                    Scram scram = scramDbh.createNew(dbc, user.getId(), username, data);
                    dbc.commit();
                    return new UserScram(user, scram);
                } catch (SQLException e) {
                    dbc.rollback();
                    throw e;
                } finally {
                    dbc.setAutoCommit(true);
                }
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
    public NewNamedResult createNewNamed(Connection dbc,
                                         UserDbh userDbh,
                                         ScramDbh scramDbh,
                                         ScreenNameDbh screenNameDbh,
                                         String username,
                                         ScramUtils.NewPasswordStringData data,
                                         String screenName) throws SQLException {


        try {
            String sqlLock = "LOCK TABLES users WRITE, user_scram WRITE, user_screen_names WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (scramDbh.usernameExists(dbc, username)) {
                return new NewNamedResult(false, null, true);
            }


            if (screenNameDbh.exists(dbc, screenName)) {
                return new NewNamedResult(false, null, false);
            }


            try {
                dbc.setAutoCommit(false);

                User user = userDbh.createNew(dbc, false, UserLoginType.SCRAM);
                Scram scram = scramDbh.createNew(dbc, user.getId(), username, data);

                screenNameDbh.createNew(dbc, user.getId(), screenName);
                dbc.commit();

                return new NewNamedResult(true, new UserScram(user, scram), false);
            } catch (Exception e) {
                dbc.rollback();
                throw e;
            } finally {
                dbc.setAutoCommit(true);
            }
        } finally {
            String sqlLock = "UNLOCK TABLES";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);
        }
    }


    @Override
    public boolean replaceExisting(Connection dbc,
                                   ScramDbh scramDbh,
                                   ScreenNameDbh screenNameDbh,
                                   long userId,
                                   String newUsername,
                                   ScramUtils.NewPasswordStringData data,
                                   String screenName) throws SQLException {

        try {
            String sqlLock = "LOCK TABLES user_scram WRITE, user_screen_names WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (screenNameDbh.exists(dbc, screenName)) {
                return false;
            }

            try {
                dbc.setAutoCommit(false);

                replaceExistingNamed(dbc, scramDbh, userId, newUsername, data);
                screenNameDbh.createNew(dbc, userId, screenName);
                dbc.commit();

                return true;
            } catch (Exception e) {
                dbc.rollback();
                throw e;
            } finally {
                dbc.setAutoCommit(true);
            }
        } finally {
            String sqlLock = "UNLOCK TABLES";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);
        }
    }


    @Override
    public void replaceExistingNamed(Connection dbc,
                                     ScramDbh scramDbh,
                                     long userId,
                                     String newUsername,
                                     ScramUtils.NewPasswordStringData data) throws SQLException {

        scramDbh.replace(dbc, userId,
                newUsername,
                data);
    }
}
