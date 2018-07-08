package com.bolyartech.forge.server.module.user_scram.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.misc.Params;
import com.bolyartech.forge.server.module.user.ForgeUserDbEndpoint;
import com.bolyartech.forge.server.module.user.UserResponseCodes;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenName;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.UserScramUtils;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbh;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.MissingParametersResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.scram_sasl.common.ScramUtils;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.SQLException;


public class RegistrationPostAutoEp extends ForgeUserDbEndpoint {
    static final String PARAM_NEW_USERNAME = "new_username";
    static final String PARAM_NEW_PASSWORD = "new_password";
    static final String PARAM_SCREEN_NAME = "screen_name";

    private final Gson gson;

    private final UserDbh userDbh;
    private final ScramDbh scramDbh;
    private final UserScramDbh userScramDbh;
    private final ScreenNameDbh screenNameDbh;


    public RegistrationPostAutoEp(DbPool dbPool,
                                  UserDbh userDbh,
                                  ScramDbh scramDbh,
                                  UserScramDbh userScramDbh,
                                  ScreenNameDbh screenNameDbh) {

        super(dbPool);
        gson = new Gson();
        this.userDbh = userDbh;
        this.scramDbh = scramDbh;
        this.userScramDbh = userScramDbh;
        this.screenNameDbh = screenNameDbh;
    }


    @Override
    public ForgeResponse handle(RequestContext ctx,
                                Connection dbc,
                                User user) throws ResponseException, SQLException {

        String newUsername = ctx.getFromPost(PARAM_NEW_USERNAME);
        String newPassword = ctx.getFromPost(PARAM_NEW_PASSWORD);
        String screenName = ctx.getFromPost(PARAM_SCREEN_NAME);

        if (!Params.areAllPresent(newUsername, newPassword)) {
            return MissingParametersResponse.getInstance();
        }

        ScreenName existingScreenName = screenNameDbh.loadByUser(dbc, user.getId());
        if (existingScreenName == null) {
            if (Strings.isNullOrEmpty(screenName)) {
                return new MissingParametersResponse("missing screen name");
            } else if (!ScreenName.isValid(screenName)) {
                return new ForgeResponse(UserResponseCodes.Errors.INVALID_SCREEN_NAME, "Invalid screen name");
            }
        }


        if (!User.isValidUsername(newUsername)) {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_USERNAME, "Invalid username");
        }

        if (!User.isValidPasswordLength(newPassword)) {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_PASSWORD, "Password too short");
        }


        ScramUtils.NewPasswordStringData data = UserScramUtils.createPasswordData(newPassword);

        boolean rez;
        if (existingScreenName == null) {
            rez = userScramDbh.replaceExisting(dbc, scramDbh, screenNameDbh,
                    user.getId(), newUsername, data, screenName);
        } else {
            userScramDbh.replaceExistingNamed(dbc, scramDbh,
                    user.getId(), newUsername, data);
            rez = true;
        }

        if (rez) {
            return new OkResponse();
        } else {
            return new ForgeResponse(UserResponseCodes.Errors.SCREEN_NAME_EXISTS, "Scree name already taken");
        }
    }


}
