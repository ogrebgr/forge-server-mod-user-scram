package com.bolyartech.forge.server.module.user_scram.data.scram;

import com.bolyartech.scram_sasl.common.ScramUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class UserScramUtils {
    public static final int DEFAULT_ITERATIONS = 4096;
    public static final String DEFAULT_HMAC = "HmacSHA512";
    public static final String DEFAULT_DIGEST = "SHA-512";


    public static ScramUtils.NewPasswordStringData createPasswordData(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[24];
            random.nextBytes(salt);

            return ScramUtils.byteArrayToStringData(
                    ScramUtils.newPassword(password,
                            salt,
                            DEFAULT_ITERATIONS,
                            DEFAULT_DIGEST,
                            DEFAULT_HMAC
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
