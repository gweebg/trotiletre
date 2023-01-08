package org.trotiletre.common;

import java.io.IOException;

public interface IAuthenticationManager {

    /**
     * Registers a new user with the given username and password.
     * <p>
     * This method sends a request to the authentication manager service to register the user.
     *
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @throws IOException If an error occurs while sending the request.
     */
    public void registerUser(String username, String password) throws IOException;

    public void loginUser(String username, String password) throws IOException;

}