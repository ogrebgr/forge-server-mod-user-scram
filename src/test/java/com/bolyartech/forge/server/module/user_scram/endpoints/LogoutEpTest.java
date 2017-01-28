package com.bolyartech.forge.server.module.user_scram.endpoints;

import com.bolyartech.forge.server.module.user.SessionVars;
import com.bolyartech.forge.server.module.user.data.UserLoginType;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.endpoints.LogoutEp;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.forge.server.session.Session;
import com.bolyartech.forge.server.session.TestSession;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LogoutEpTest {
    @Test
    public void test() throws ResponseException {
        LogoutEp ep = new LogoutEp();

        RequestContext rc = mock(RequestContext.class);
        Session session = new TestSession();
        when(rc.getSession()).thenReturn(session);

        session.setVar(SessionVars.VAR_USER, new User(11, false, UserLoginType.FACEBOOK.getCode()));
        ep.handleForge(rc);

        assertTrue("user session var not cleared", session.getVar(SessionVars.VAR_USER) == null);
    }


}
