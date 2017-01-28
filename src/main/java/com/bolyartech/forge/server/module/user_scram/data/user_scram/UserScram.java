package com.bolyartech.forge.server.module.user_scram.data.user_scram;

import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user_scram.data.scram.Scram;


public final class UserScram {
    private final User mUser;
    private final Scram mScram;


    public UserScram(User user, Scram scram) {
        mUser = user;
        mScram = scram;
    }


    public User getUser() {
        return mUser;
    }


    public Scram getScram() {
        return mScram;
    }

}
