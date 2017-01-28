package com.bolyartech.forge.server.module.user_scram.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.module.user.UserResponseCodes;
import com.bolyartech.forge.server.module.user.data.UserLoginType;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenName;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.forge.server.module.user_scram.data.user_scram.UserScramDbh;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.BasicResponseCodes;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.forge.server.session.Session;
import com.bolyartech.forge.server.session.TestSession;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RegistrationPostAutoEpTest {
    @Test
    public void testMissingParameters() throws ResponseException, SQLException {
        DbPool dbPool = mock(DbPool.class);
        UserDbh userDbh = mock(UserDbh.class);
        ScramDbh scramDbh = mock(ScramDbh.class);
        UserScramDbh userScramDbh = mock(UserScramDbh.class);
        ScreenNameDbh screenNameDbh = mock(ScreenNameDbh.class);
        User user = new User(11, false, UserLoginType.SCRAM.getCode());

        RegistrationPostAutoEp ep = new RegistrationPostAutoEp(dbPool, userDbh, scramDbh, userScramDbh, screenNameDbh);

        RequestContext rc = mock(RequestContext.class);
        Session session = new TestSession();
        Connection dbc = mock(Connection.class);

        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn(null);
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("some_password");
        ForgeResponse resp = ep.handle(rc, dbc, user);
        assertTrue("Unexpected code", resp.getResultCode() == BasicResponseCodes.Errors.MISSING_PARAMETERS.getCode());

        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn(null);
        resp = ep.handle(rc, dbc, user);
        assertTrue("Unexpected code", resp.getResultCode() == BasicResponseCodes.Errors.MISSING_PARAMETERS.getCode());

        when(screenNameDbh.loadByUser(any(), anyLong())).thenReturn(null);
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("some_password");
        when(rc.getFromPost(RegistrationEp.PARAM_SCREEN_NAME)).thenReturn(null);
        resp = ep.handle(rc, dbc, user);
        assertTrue("Unexpected code", resp.getResultCode() == BasicResponseCodes.Errors.MISSING_PARAMETERS.getCode());
    }


    @Test
    public void testInvalidParameters() throws ResponseException, SQLException {
        DbPool dbPool = mock(DbPool.class);
        UserDbh userDbh = mock(UserDbh.class);
        ScramDbh scramDbh = mock(ScramDbh.class);
        UserScramDbh userScramDbh = mock(UserScramDbh.class);
        ScreenNameDbh screenNameDbh = mock(ScreenNameDbh.class);
        User user = new User(11, false, UserLoginType.SCRAM.getCode());

        RegistrationPostAutoEp ep = new RegistrationPostAutoEp(dbPool, userDbh, scramDbh, userScramDbh, screenNameDbh);

        RequestContext rc = mock(RequestContext.class);
        Session session = new TestSession();
        Connection dbc = mock(Connection.class);

        when(screenNameDbh.loadByUser(any(), anyLong())).thenReturn(null);
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("some_password");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_SCREEN_NAME)).thenReturn("1as");
        ForgeResponse resp = ep.handle(rc, dbc, user);
        assertTrue("Unexpected code", resp.getResultCode() == UserResponseCodes.Errors.INVALID_SCREEN_NAME.getCode());

        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("12username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("some_password");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_SCREEN_NAME)).thenReturn("some screen name");
        resp = ep.handle(rc, dbc, user);
        assertTrue("Unexpected code", resp.getResultCode() == UserResponseCodes.Errors.INVALID_USERNAME.getCode());

        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("somwod");
        when(rc.getFromPost(RegistrationEp.PARAM_SCREEN_NAME)).thenReturn("some screen name");
        resp = ep.handle(rc, dbc, user);
        assertTrue("Unexpected code", resp.getResultCode() == UserResponseCodes.Errors.INVALID_PASSWORD.getCode());
    }


    @Test
    public void testOk1() throws SQLException, ResponseException {
        DbPool dbPool = mock(DbPool.class);
        UserDbh userDbh = mock(UserDbh.class);
        ScramDbh scramDbh = mock(ScramDbh.class);
        UserScramDbh userScramDbh = mock(UserScramDbh.class);
        ScreenNameDbh screenNameDbh = mock(ScreenNameDbh.class);
        User user = new User(11, false, UserLoginType.SCRAM.getCode());

        RegistrationPostAutoEp ep = new RegistrationPostAutoEp(dbPool, userDbh, scramDbh, userScramDbh, screenNameDbh);

        RequestContext rc = mock(RequestContext.class);
        Session session = new TestSession();
        Connection dbc = mock(Connection.class);

        when(screenNameDbh.loadByUser(any(), anyLong())).thenReturn(null);
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("some_password");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_SCREEN_NAME)).thenReturn("some screen name");

        when(userScramDbh.replaceExisting(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(true);
        ForgeResponse resp = ep.handle(rc, dbc, user);

        assertTrue("Unexpected response", resp instanceof OkResponse);
    }


    @Test
    public void testScreenNameTaken() throws SQLException, ResponseException {
        DbPool dbPool = mock(DbPool.class);
        UserDbh userDbh = mock(UserDbh.class);
        ScramDbh scramDbh = mock(ScramDbh.class);
        UserScramDbh userScramDbh = mock(UserScramDbh.class);
        ScreenNameDbh screenNameDbh = mock(ScreenNameDbh.class);
        User user = new User(11, false, UserLoginType.SCRAM.getCode());

        RegistrationPostAutoEp ep = new RegistrationPostAutoEp(dbPool, userDbh, scramDbh, userScramDbh, screenNameDbh);

        RequestContext rc = mock(RequestContext.class);
        Session session = new TestSession();
        Connection dbc = mock(Connection.class);

        when(screenNameDbh.loadByUser(any(), anyLong())).thenReturn(null);
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("some_password");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_SCREEN_NAME)).thenReturn("some screen name");

        when(userScramDbh.replaceExisting(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(false);
        ForgeResponse resp = ep.handle(rc, dbc, user);

        assertTrue("Unexpected code", resp.getResultCode() == UserResponseCodes.Errors.SCREEN_NAME_EXISTS.getCode());
    }


    @Test
    public void testOk2() throws SQLException, ResponseException {
        DbPool dbPool = mock(DbPool.class);
        UserDbh userDbh = mock(UserDbh.class);
        ScramDbh scramDbh = mock(ScramDbh.class);
        UserScramDbh userScramDbh = mock(UserScramDbh.class);
        ScreenNameDbh screenNameDbh = mock(ScreenNameDbh.class);
        User user = new User(11, false, UserLoginType.SCRAM.getCode());

        RegistrationPostAutoEp ep = new RegistrationPostAutoEp(dbPool, userDbh, scramDbh, userScramDbh, screenNameDbh);

        RequestContext rc = mock(RequestContext.class);
        Session session = new TestSession();
        Connection dbc = mock(Connection.class);

        when(screenNameDbh.loadByUser(any(), anyLong())).thenReturn(new ScreenName(11, "dsf"));
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_USERNAME)).thenReturn("username");
        when(rc.getFromPost(RegistrationPostAutoEp.PARAM_NEW_PASSWORD)).thenReturn("some_password");

        when(userScramDbh.replaceExisting(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(true);
        ForgeResponse resp = ep.handle(rc, dbc, user);

        assertTrue("Unexpected response", resp instanceof OkResponse);
    }
}
