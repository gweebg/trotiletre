package org.trotiletre.server.services;

import org.trotiletre.common.IAuthenticationManager;
import org.trotiletre.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AuthenticationManager implements IAuthenticationManager {

    private Map<String, Boolean> onlineAccounts = new HashMap<>();
    private Map<String, User> accounts = new HashMap<>();
    private ReentrantLock authLock = new ReentrantLock();

    public AuthenticationManager() {}

    public boolean isUserOnline(String username) {

        try {

            authLock.lock();
            return onlineAccounts.getOrDefault(username, false);

        } finally {
            authLock.unlock();
        }

    }

    public void registerUser(String username, String passwordHash) {

        try {

            authLock.lock();

            User newUser = new User(username, passwordHash);
            if (!accounts.containsKey(username)) accounts.put(username, newUser);

        } finally {
            authLock.unlock();
        }
    }

    public void loginUser(String username, String password) {

        try {

            authLock.lock();

            User user = accounts.get(username);
            if (user != null && user.matchPassword(password)) onlineAccounts.put(username, true);

        } finally {
            authLock.unlock();
        }

    }

    public User getUser(String username) {

        try {

            authLock.lock();
            return accounts.get(username);

        } finally { authLock.unlock(); }
    }

}
