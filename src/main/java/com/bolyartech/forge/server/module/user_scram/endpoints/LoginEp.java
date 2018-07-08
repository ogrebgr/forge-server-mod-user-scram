package com.bolyartech.forge.server.module.user_scram.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.handler.ForgeDbSecureEndpoint;
import com.bolyartech.forge.server.misc.Params;
import com.bolyartech.forge.server.module.user.LoginType;
import com.bolyartech.forge.server.module.user.SessionVars;
import com.bolyartech.forge.server.module.user.UserResponseCodes;
import com.bolyartech.forge.server.module.user.data.SessionInfo;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenName;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.Scram;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.UserScramUtils;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.InvalidParameterValueResponse;
import com.bolyartech.forge.server.response.forge.MissingParametersResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.forge.server.session.Session;
import com.bolyartech.scram_sasl.common.ScramException;
import com.bolyartech.scram_sasl.server.ScramServerFunctionality;
import com.bolyartech.scram_sasl.server.ScramServerFunctionalityImpl;
import com.bolyartech.scram_sasl.server.UserData;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.sql.Connection;
import java.sql.SQLException;


public class LoginEp extends ForgeDbSecureEndpoint {
    static final String PARAM_STEP = "step";
    static final String PARAM_DATA = "data";

    private final UserDbh userDbh;
    private final ScramDbh scramDbh;
    private final ScreenNameDbh screenNameDbh;

    private final Gson gson;


    public LoginEp(DbPool dbPool, UserDbh userDbh, ScramDbh scramDbh, ScreenNameDbh screenNameDbh) {
        super(dbPool);
        this.userDbh = userDbh;
        this.scramDbh = scramDbh;
        this.screenNameDbh = screenNameDbh;
        gson = new Gson();
    }


    @Override
    public ForgeResponse handleForgeSecure(RequestContext ctx, Connection dbc)
            throws ResponseException, SQLException {

        String stepStr = ctx.getFromPost(PARAM_STEP);
        String data = ctx.getFromPost(PARAM_DATA);
        if (Params.areAllPresent(stepStr, data)) {
            try {
                Session session = ctx.getSession();
                int step = Integer.parseInt(stepStr);
                if (step == 1) {
                    return handleStep1(dbc, session, data);
                } else if (step == 2) {
                    return handleStep2(dbc, session, data);
                } else {
                    return new InvalidParameterValueResponse("invalid step");
                }
            } catch (NumberFormatException e) {
                return new InvalidParameterValueResponse("step not integer");
            }
        } else {
            return MissingParametersResponse.getInstance();
        }
    }


    private ForgeResponse handleStep2(Connection dbc, Session session, String data) throws SQLException {
        ScramServerFunctionality scram = session.getVar(SessionVars.VAR_SCRAM_FUNC);
        if (scram != null) {
            if (scram.getState() == ScramServerFunctionality.State.PREPARED_FIRST) {
                try {
                    String finalMsg = scram.prepareFinalMessage(data);
                    if (finalMsg != null) {
                        Scram scramData = session.getVar(SessionVars.VAR_SCRAM_DATA);

                        SessionInfo si = createSessionInfo(dbc, scramData.getUser());

                        User user = userDbh.loadById(dbc, scramData.getUser());
                        session.setVar(SessionVars.VAR_USER, user);
                        session.setVar(SessionVars.VAR_LOGIN_TYPE, LoginType.NATIVE);
                        return new OkResponse(gson.toJson(new RokLogin(session.getMaxInactiveInterval(), si, finalMsg)));
                    } else {
                        return new ForgeResponse(UserResponseCodes.Errors.INVALID_LOGIN, "Invalid login");
                    }
                } catch (ScramException e) {
                    return new ForgeResponse(UserResponseCodes.Errors.INVALID_LOGIN, "Invalid login");
                }
            } else {
                session.setVar(SessionVars.VAR_SCRAM_FUNC, null);
                return new ForgeResponse(UserResponseCodes.Errors.INVALID_LOGIN, "Invalid login");
            }
        } else {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_LOGIN, "Invalid login");
        }
    }


    private ForgeResponse handleStep1(Connection dbc, Session session, String data) {
        // first remove existing old functionality if any
        session.setVar(SessionVars.VAR_SCRAM_FUNC, null);

        ScramServerFunctionality scram = new ScramServerFunctionalityImpl(UserScramUtils.DEFAULT_DIGEST,
                UserScramUtils.DEFAULT_HMAC);
        String username = scram.handleClientFirstMessage(data);

        if (username != null) {
            try {
                Scram scramData = scramDbh.loadByUsername(dbc, username);
                if (scramData != null) {
                    session.setVar(SessionVars.VAR_SCRAM_DATA, scramData);
                    UserData ud = new UserData(scramData.getSalt(), scramData.getIterations(),
                            scramData.getServerKey(), scramData.getStoredKey());
                    String first = scram.prepareFirstMessage(ud);

                    session.setVar(SessionVars.VAR_SCRAM_FUNC, scram);
                    return new OkResponse(first);
                } else {
                    return new ForgeResponse(UserResponseCodes.Errors.INVALID_LOGIN, "Invalid Login");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new InvalidParameterValueResponse("Invalid data");
        }
    }


    private SessionInfo createSessionInfo(Connection dbc, long userId) throws SQLException {
        ScreenName sn = screenNameDbh.loadByUser(dbc, userId);

        SessionInfo si;
        if (sn != null) {
            si = new SessionInfo(userId, sn.getScreenName());
        } else {
            si = new SessionInfo(userId, null);
        }

        return si;
    }


    public static class RokLogin {
        @SerializedName("session_ttl")
        public final int sessionTtl;
        @SerializedName("session_info")
        public final SessionInfo sessionInfo;
        @SerializedName("final_message")
        public final String finalMessage;


        public RokLogin(int sessionTtl, SessionInfo sessionInfo, String finalMessage) {
            this.sessionTtl = sessionTtl;
            this.sessionInfo = sessionInfo;
            this.finalMessage = finalMessage;
        }
    }
}
