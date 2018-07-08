package com.bolyartech.forge.server.module.user_scram.data.scram;

import com.google.common.base.Strings;

import java.util.Objects;


public final class Scram {
    private final long user;
    private final String username;
    private final String salt;
    private final String serverKey;
    private final String storedKey;
    private final int iterations;


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

        this.user = user;
        this.username = username;
        this.salt = salt;
        this.serverKey = serverKey;
        this.storedKey = storedKey;
        this.iterations = iterations;
    }


    public static boolean isValidUsername(String username) {
        return username.matches("^[\\p{L}][\\p{L}\\p{N} _]{1,48}[\\p{L}\\p{N}]$");
    }


    public long getUser() {
        return user;
    }


    public String getUsername() {
        return username;
    }


    public String getSalt() {
        return salt;
    }


    public String getServerKey() {
        return serverKey;
    }


    public String getStoredKey() {
        return storedKey;
    }


    public int getIterations() {
        return iterations;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Scram) {
            Scram other = (Scram) obj;

            return user == other.getUser() && username.equals(other.getUsername()) &&
                    serverKey.equals(other.getServerKey()) && storedKey.equals(other.getStoredKey()) &&
                    salt.equals(other.getSalt()) && iterations == other.getIterations();
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(user, username, salt, serverKey, storedKey, iterations);
    }

}
