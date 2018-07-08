package com.bolyartech.forge.server.module.user_scram;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.module.HttpModule;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbhImpl;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user.data.user.UserDbhImpl;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbhImpl;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbhImpl;
import com.bolyartech.forge.server.module.user_scram.endpoints.AutoregistrationEp;
import com.bolyartech.forge.server.module.user_scram.endpoints.LoginEp;
import com.bolyartech.forge.server.module.user_scram.endpoints.RegistrationEp;
import com.bolyartech.forge.server.module.user_scram.endpoints.RegistrationPostAutoEp;
import com.bolyartech.forge.server.route.PostRoute;
import com.bolyartech.forge.server.route.Route;

import java.util.ArrayList;
import java.util.List;


public class UserScramModule implements HttpModule {
    private static final String DEFAULT_PATH_PREFIX = "/api/user/";

    private static final String MODULE_SYSTEM_NAME = "user_scram";
    private static final int MODULE_VERSION_CODE = 1;
    private static final String MODULE_VERSION_NAME = "1.0.0";

    private final String pathPrefix;
    private final DbPool dbPool;
    private final UserScramDbh userScramDbh;
    private final UserDbh userDbh;
    private final ScramDbh scramDbh;
    private final ScreenNameDbh screenNameDbh;

    public static UserScramModule createDefault(DbPool dbPool) {
        return new UserScramModule(dbPool,
                new UserScramDbhImpl(),
                new UserDbhImpl(),
                new ScramDbhImpl(),
                new ScreenNameDbhImpl());
    }


    public UserScramModule(String pathPrefix, DbPool dbPool, UserScramDbh userScramDbh, UserDbh userDbh, ScramDbh scramDbh,
                           ScreenNameDbh screenNameDbh) {

        this.pathPrefix = pathPrefix;
        this.dbPool = dbPool;
        this.userScramDbh = userScramDbh;
        this.userDbh = userDbh;
        this.scramDbh = scramDbh;
        this.screenNameDbh = screenNameDbh;
    }


    public UserScramModule(DbPool dbPool, UserScramDbh userScramDbh, UserDbh userDbh, ScramDbh scramDbh,
                           ScreenNameDbh screenNameDbh) {

        this(DEFAULT_PATH_PREFIX, dbPool, userScramDbh, userDbh, scramDbh, screenNameDbh);
    }


    @Override
    public List<Route> createRoutes() {
        List<Route> ret = new ArrayList<>();

        ret.add(new PostRoute(pathPrefix + "autoregister",
                new AutoregistrationEp(dbPool, userDbh, scramDbh, userScramDbh)));
        ret.add(new PostRoute(pathPrefix + "login",
                new LoginEp(dbPool, userDbh, scramDbh, screenNameDbh)));
        ret.add(new PostRoute(pathPrefix + "register",
                new RegistrationEp(dbPool, userDbh, scramDbh, userScramDbh, screenNameDbh)));
        ret.add(new PostRoute(pathPrefix + "register_postauto",
                new RegistrationPostAutoEp(dbPool, userDbh, scramDbh, userScramDbh, screenNameDbh)));

        return ret;
    }


    @Override
    public String getSystemName() {
        return MODULE_SYSTEM_NAME;
    }


    @Override
    public String getShortDescription() {
        return "";
    }


    @Override
    public int getVersionCode() {
        return MODULE_VERSION_CODE;
    }


    @Override
    public String getVersionName() {
        return MODULE_VERSION_NAME;
    }
}
