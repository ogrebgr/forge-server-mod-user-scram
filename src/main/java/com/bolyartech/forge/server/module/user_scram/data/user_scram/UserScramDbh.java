package com.bolyartech.forge.server.module.user_scram.data.user_scram;

import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_scram.data.scram.ScramDbh;
import com.bolyartech.scram_sasl.common.ScramUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public interface UserScramDbh {
    UserScram createNew(Connection dbc,
                        UserDbh userDbh,
                        ScramDbh scramDbh,
                        String username,
                        ScramUtils.NewPasswordStringData data) throws SQLException;


    NewNamedResult createNewNamed(Connection dbc,
                                  UserDbh userDbh,
                                  ScramDbh scramDbh,
                                  ScreenNameDbh screenNameDbh,
                                  String username,
                                  ScramUtils.NewPasswordStringData data,
                                  String screenName) throws SQLException;

    boolean replaceExisting(Connection dbc,
                            ScramDbh scramDbh,
                            ScreenNameDbh screenNameDbh,
                            long userId,
                            String newUsername,
                            ScramUtils.NewPasswordStringData data,
                            String screenName) throws SQLException;

    void replaceExistingNamed(Connection dbc,
                              ScramDbh scramDbh,
                              long userId,
                              String newUsername,
                              ScramUtils.NewPasswordStringData data) throws SQLException;


    class NewNamedResult {
        public final boolean isOk;
        public final UserScram mUserScram;
        public final boolean usernameExist;


        public NewNamedResult(boolean isOk, UserScram userScram, boolean usernameExist) {
            this.isOk = isOk;
            mUserScram = userScram;
            this.usernameExist = usernameExist;
        }
    }
}
