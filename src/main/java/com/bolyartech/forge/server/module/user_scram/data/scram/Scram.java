package com.bolyartech.forge.server.module.user_scram.data.scram;

import com.google.common.base.Strings;

import java.util.Objects;


public final class Scram {
    private final long mUser;
    private final String mUsername;
    private final String mSalt;
    private final String mServerKey;
    private final String mStoredKey;
    private final int mIterations;


    public Scram(long user, String username, String salt, String serverKey, String storedKey, int iterations) {
        if (user <= 0) {
            throw new IllegalArgumentException("user <= 0: " + user);
        }

        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalArgumentException("username is empty");
        }

        if (Strings.isNullOrEmpty(salt)) {
            throw new IllegalArgumentException("salt is empty");
        }
        if (Strings.isNullOrEmpty(serverKey)) {
            throw new IllegalArgumentException("serverKey is empty");
        }
        if (Strings.isNullOrEmpty(storedKey)) {
            throw new IllegalArgumentException("storedKey is empty");
        }
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations <= 0");
        }

        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username: " + username);
        }

        mUser = user;
        mUsername = username;
        mSalt = salt;
        mServerKey = serverKey;
        mStoredKey = storedKey;
        mIterations = iterations;
    }


    public static boolean isValidUsername(String username) {
        return username.matches("^[\\p{L}][\\p{L}\\p{N} _]{1,48}[\\p{L}\\p{N}]$");
    }


    public long getUser() {
        return mUser;
    }


    public String getUsername() {
        return mUsername;
    }


    public String getSalt() {
        return mSalt;
    }


    public String getServerKey() {
        return mServerKey;
    }


    public String getStoredKey() {
        return mStoredKey;
    }


    public int getIterations() {
        return mIterations;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Scram) {
            Scram other = (Scram) obj;

            return mUser == other.getUser() && mUsername.equals(other.getUsername()) &&
                    mServerKey.equals(other.getServerKey()) && mStoredKey.equals(other.getStoredKey()) &&
                    mSalt.equals(other.getSalt()) && mIterations == other.getIterations();
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(mUser, mUsername, mSalt, mServerKey, mStoredKey, mIterations);
    }

}
