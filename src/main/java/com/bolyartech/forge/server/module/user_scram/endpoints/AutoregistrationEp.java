package com.bolyartech.forge.server.module.user_scram.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.handler.ForgeDbEndpoint;
import com.bolyartech.forge.server.module.user.LoginType;
import com.bolyartech.forge.server.module.user.SessionVars;
import com.bolyartech.forge.server.module.user.data.RokResponseAutoregistration;
import com.bolyartech.forge.server.module.user.data.SessionInfo;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.UserScramUtils;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScram;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbh;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.forge.server.session.Session;
import com.bolyartech.scram_sasl.common.ScramUtils;
import com.google.gson.Gson;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;


public class AutoregistrationEp extends ForgeDbEndpoint {
    private final Gson gson;

    private final UserDbh userDbh;
    private final ScramDbh scramDbh;
    private final UserScramDbh userScramDbh;


    public AutoregistrationEp(DbPool dbPool, UserDbh userDbh, ScramDbh scramDbh, UserScramDbh userScramDbh) {
        super(dbPool);
        gson = new Gson();
        this.userDbh = userDbh;
        this.scramDbh = scramDbh;
        this.userScramDbh = userScramDbh;
    }


    @Override
    public ForgeResponse handleForge(RequestContext ctx, Connection dbc) throws ResponseException,
            SQLException {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[24];
        random.nextBytes(salt);

        String username;
        String password = UUID.randomUUID().toString();
        UserScram us;

        while (true) {
            // adding "g" as a prefix in order to make the username valid when UUID starts with number
            username = "g" + UUID.randomUUID().toString().replace("-", "");

            try {
                ScramUtils.NewPasswordStringData data = ScramUtils.byteArrayToStringData(
                        ScramUtils.newPassword(password, salt, UserScramUtils.DEFAULT_ITERATIONS,
                                UserScramUtils.DEFAULT_DIGEST,
                                UserScramUtils.DEFAULT_HMAC
                        )
                );

                us = userScramDbh.createNew(dbc, userDbh, scramDbh, username, data);
                if (us != null) {
                    break;
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

        SessionInfo si = new SessionInfo(us.getUser().getId(), null);

        Session session = ctx.getSession();
        session.setVar(SessionVars.VAR_USER, us.getUser());
        session.setVar(SessionVars.VAR_LOGIN_TYPE, LoginType.NATIVE);
        return new OkResponse(
                gson.toJson(new RokResponseAutoregistration(username,
                        password,
                        session.getMaxInactiveInterval(),
                        si
                )));
    }
}
