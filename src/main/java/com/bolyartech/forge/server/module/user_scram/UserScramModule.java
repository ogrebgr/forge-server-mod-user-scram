package com.bolyartech.forge.server.module.user_scram;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.module.HttpModule;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbh;
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

    private final String mPathPrefix;
    private final DbPool mDbPool;
    private final UserScramDbh mUserScramDbh;
    private final UserDbh mUserDbh;
    private final ScramDbh mScramDbh;
    private final ScreenNameDbh mScreenNameDbh;


    public UserScramModule(String pathPrefix, DbPool dbPool, UserScramDbh userScramDbh, UserDbh userDbh, ScramDbh scramDbh,
                           ScreenNameDbh screenNameDbh) {

        mPathPrefix = pathPrefix;
        mDbPool = dbPool;
        mUserScramDbh = userScramDbh;
        mUserDbh = userDbh;
        mScramDbh = scramDbh;
        mScreenNameDbh = screenNameDbh;
    }


    public UserScramModule(DbPool dbPool, UserScramDbh userScramDbh, UserDbh userDbh, ScramDbh scramDbh,
                           ScreenNameDbh screenNameDbh) {

        this(DEFAULT_PATH_PREFIX, dbPool, userScramDbh, userDbh, scramDbh, screenNameDbh);
    }


    @Override
    public List<Route> createRoutes() {
        List<Route> ret = new ArrayList<>();

        ret.add(new PostRoute(mPathPrefix + "autoregister",
                new AutoregistrationEp(mDbPool, mUserDbh, mScramDbh, mUserScramDbh)));
        ret.add(new PostRoute(mPathPrefix + "login",
                new LoginEp(mDbPool, mUserDbh, mScramDbh, mScreenNameDbh)));
        ret.add(new PostRoute(mPathPrefix + "register",
                new RegistrationEp(mDbPool, mUserDbh, mScramDbh, mUserScramDbh, mScreenNameDbh)));
        ret.add(new PostRoute(mPathPrefix + "register_postauto",
                new RegistrationPostAutoEp(mDbPool, mUserDbh, mScramDbh, mUserScramDbh, mScreenNameDbh)));

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
