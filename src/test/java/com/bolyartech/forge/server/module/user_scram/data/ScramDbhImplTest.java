package com.bolyartech.forge.server.module.user_scram.data;

import com.bolyartech.forge.server.config.ForgeConfigurationException;
import com.bolyartech.forge.server.db.*;
import com.bolyartech.forge.server.module.user.data.UserLoginType;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user.data.user.UserDbhImpl;
import com.bolyartech.forge.server.module.user_scram.DbTools;
import com.bolyartech.forge.server.module.user_scram.data.scram.Scram;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbhImpl;
import com.bolyartech.scram_sasl.common.ScramUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;


@SuppressWarnings("ConstantConditions")
public class ScramDbhImplTest {
    private DbPool mDbPool;


    @Before
    public void setup() throws SQLException, ForgeConfigurationException {
        if (mDbPool == null) {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("conf/db.conf").getFile());

            DbConfigurationLoader loader = new FileDbConfigurationLoader();
            DbConfiguration dbConf = loader.load();

            mDbPool = DbUtils.createC3P0DbPool(dbConf);
        }

        Connection dbc = mDbPool.getConnection();
        DbTools.deleteAllScreenNames(dbc);
        DbTools.deleteAllScrams(dbc);
        DbTools.deleteAllUsers(dbc);

        dbc.close();
    }


    @After
    public void after() throws SQLException {
        Connection dbc = mDbPool.getConnection();
        DbTools.deleteAllScrams(dbc);
        dbc.close();
    }


    @Test
    public void testCreateNew() throws SQLException {
        ScramDbh dbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();

        Connection dbc = mDbPool.getConnection();
        User userNew = userDbh.createNew(dbc, true, UserLoginType.GOOGLE);

        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "salt", "clientKey",
                "server_key", "stored_key", 11);

        Scram scrNew = dbh.createNew(dbc, userNew.getId(), "username", data);
        Scram scrLoaded = dbh.loadByUser(dbc, userNew.getId());
        assertTrue("Created and loaded are different", scrLoaded.equals(scrNew));

        Scram scrNew2 = dbh.createNew(dbc, userNew.getId(), "username", data);
        assertTrue("duplicate username used", scrNew2 == null);

        dbc.close();
    }


    @Test
    public void testLoadByUsername() throws SQLException {
        ScramDbh dbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();

        Connection dbc = mDbPool.getConnection();
        User userNew = userDbh.createNew(dbc, true, UserLoginType.GOOGLE);

        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "salt", "clientKey",
                "server_key", "stored_key", 11);

        Scram scrNew = dbh.createNew(dbc, userNew.getId(), "username", data);
        Scram scrLoaded = dbh.loadByUsername(dbc, "username");
        dbc.close();

        assertTrue("Created and loaded are different", scrLoaded.equals(scrNew));
    }


    @Test
    public void testReplace() throws SQLException {
        ScramDbh dbh = new ScramDbhImpl();
        UserDbh userDbh = new UserDbhImpl();

        Connection dbc = mDbPool.getConnection();
        User userNew = userDbh.createNew(dbc, true, UserLoginType.GOOGLE);

        ScramUtils.NewPasswordStringData data = new ScramUtils.NewPasswordStringData("salted", "salt", "clientKey",
                "server_key", "stored_key", 11);
        ScramUtils.NewPasswordStringData data2 = new ScramUtils.NewPasswordStringData("salted2", "salt2", "clientKey2",
                "server_key2", "stored_key2", 11);

        Scram scrNew = dbh.createNew(dbc, userNew.getId(), "username", data);
        Scram scrChanged = dbh.replace(dbc, userNew.getId(), "newusername", data2);
        Scram scrLoaded = dbh.loadByUsername(dbc, "newusername");
        dbc.close();

        assertTrue("Changed and loaded are different", scrLoaded.equals(scrChanged));
    }
}
