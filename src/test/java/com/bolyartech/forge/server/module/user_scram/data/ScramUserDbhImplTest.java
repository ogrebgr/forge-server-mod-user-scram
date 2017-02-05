package com.bolyartech.forge.server.module.user_scram.data;

import com.bolyartech.forge.server.config.ForgeConfigurationException;
import com.bolyartech.forge.server.db.*;
import com.bolyartech.forge.server.module.user.data.UserLoginType;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenName;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbhImpl;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user.data.user.UserDbhImpl;
import com.bolyartech.forge.server.module.user_scram.DbTools;
import com.bolyartech.forge.server.module.user_scram.data.scram.Scram;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbhImpl;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScram;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbhImpl;
import com.bolyartech.scram_sasl.common.ScramUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;


@SuppressWarnings("ConstantConditions")
public class ScramUserDbhImplTest {
    private DbPool mDbPool;


    @Before
    public void setup() throws SQLException, ForgeConfigurationException {
        if (mDbPool == null) {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("db.conf").getFile());
            DbConfigurationLoader loader = new FileDbConfigurationLoader(file.getParent());

            DbConfiguration dbConf = loader.load();

            mDbPool = DbUtils.createC3P0DbPool(dbConf);
        }

        Connection dbc = mDbPool.getConnection();
        DbTools.deleteAllScreenNames(dbc);
        DbTools.deleteAllScrams(dbc);
        DbTools.deleteAllUsers(dbc);

        dbc.close();
    }


    @Test
    public void testCreateNew() throws SQLException {
        ScramDbh scramDbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();

        UserScramDbh dbh = new UserScramDbhImpl();

        Connection dbc = mDbPool.getConnection();
        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "salt", "clientKey",
                "server_key", "stored_key", 11);

        UserScram scrNew = dbh.createNew(dbc, userDbh, scramDbh, "username", data);
        dbc.close();

        assertTrue("user not set", scrNew.getUser() != null);
        assertTrue("scram not set", scrNew.getScram() != null);
    }


    @Test
    public void testCreateNewNamedOk() throws SQLException {
        ScramDbh scramDbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();
        ScreenNameDbh screenNameDbh = new ScreenNameDbhImpl();

        UserScramDbh dbh = new UserScramDbhImpl();

        Connection dbc = mDbPool.getConnection();
        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "username", "salt",
                "server_key", "stored_key", 11);

        UserScramDbh.NewNamedResult rez = dbh.createNewNamed(dbc, userDbh, scramDbh, screenNameDbh, "username", data,
                "sn1");
        dbc.close();

        assertTrue("Result not OK", rez.isOk);
        assertTrue("UserBlowfish is null", rez.mUserScram != null);
    }


    @Test
    public void testCreateNewNamedUsernameDuplicate() throws SQLException {
        ScramDbh scramDbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();
        ScreenNameDbh screenNameDbh = new ScreenNameDbhImpl();

        UserScramDbh dbh = new UserScramDbhImpl();

        Connection dbc = mDbPool.getConnection();
        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "username", "salt",
                "server_key", "stored_key", 11);

        dbh.createNew(dbc, userDbh, scramDbh, "username", data);


        UserScramDbh.NewNamedResult rez = dbh.createNewNamed(dbc, userDbh, scramDbh, screenNameDbh, "username", data,
                "sn1");
        dbc.close();

        assertTrue("Result is OK when should be not", !rez.isOk);
        assertTrue("Username is not duplicate when it should", rez.usernameExist);
    }


    @Test
    public void testCreateNewNamedScreenNameDuplicate() throws SQLException {
        ScramDbh scramDbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();
        ScreenNameDbh screenNameDbh = new ScreenNameDbhImpl();

        UserScramDbh dbh = new UserScramDbhImpl();

        Connection dbc = mDbPool.getConnection();
        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "username", "salt",
                "server_key", "stored_key", 11);

        User user = userDbh.createNew(dbc, false, UserLoginType.SCRAM);
        screenNameDbh.createNew(dbc, user.getId(), "sn1");

        UserScramDbh.NewNamedResult rez = dbh.createNewNamed(dbc, userDbh, scramDbh, screenNameDbh, "username", data,
                "sn1");
        dbc.close();

        assertTrue("Result is OK when should be not", !rez.isOk);
        assertTrue("Screen name is not duplicate when it should", !rez.usernameExist);
    }


    @Test
    public void testReplaceExistingNamed() throws SQLException {
        ScramDbh scramDbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();
        ScreenNameDbh screenNameDbh = new ScreenNameDbhImpl();

        UserScramDbh dbh = new UserScramDbhImpl();

        Connection dbc = mDbPool.getConnection();
        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "username", "salt",
                "server_key", "stored_key", 11);

        UserScramDbh.NewNamedResult rez = dbh.createNewNamed(dbc, userDbh, scramDbh, screenNameDbh, "username", data,
                "sn1");


        dbh.replaceExistingNamed(dbc, scramDbh, rez.mUserScram.getUser().getId(),
                "gele",
                data
        );

        Scram scram = scramDbh.loadByUser(dbc, rez.mUserScram.getUser().getId());

        assertTrue("username not changed", scram.getUsername().equals("gele"));
    }


    @Test
    public void testReplaceExisting() throws SQLException {
        ScramDbh scramDbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();
        ScreenNameDbh screenNameDbh = new ScreenNameDbhImpl();

        UserScramDbh dbh = new UserScramDbhImpl();

        Connection dbc = mDbPool.getConnection();
        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "username", "salt",
                "server_key", "stored_key", 11);

        UserScram rez = dbh.createNew(dbc, userDbh, scramDbh, "username", data);

        dbh.replaceExisting(dbc, scramDbh, screenNameDbh, rez.getUser().getId(),
                "gele",
                data,
                "trytkata");

        Scram scram = scramDbh.loadByUser(dbc, rez.getUser().getId());
        assertTrue("username not changed", scram.getUsername().equals("gele"));

        ScreenName sn = screenNameDbh.loadByUser(dbc, rez.getUser().getId());
        assertTrue("screen name not set correctly", sn.getScreenName().equals("trytkata"));
    }
}
