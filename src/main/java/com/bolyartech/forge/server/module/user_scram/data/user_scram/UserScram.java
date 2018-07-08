package com.bolyartech.forge.server.module.user_scram.data.user_scram;

import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user_scram.data.scram.Scram;


public final class UserScram {
    private final User user;
    private final Scram scram;


    public UserScram(User user, Scram scram) {
        this.user = user;
        this.scram = scram;
    }


    public User getUser() {
        return user;
    }


    public Scram getScram() {
        return scram;
    }

}
