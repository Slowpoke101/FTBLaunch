package net.ftb.workers;

import java.net.Proxy;

import net.ftb.log.Logger;
import net.ftb.util.ErrorUtils;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class AuthlibHelper {

    protected static String authenticateWithAuthlib (String user, String pass) {
        String ID, displayName;
        if (user != null) {
            Logger.logInfo("Beginning authlib authentication attempt");
            YggdrasilUserAuthentication authentication = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
            Logger.logInfo("successfully created YggdrasilAuthenticationService");
            authentication.setUsername(user);
            authentication.setPassword(pass);
            if (!authentication.isLoggedIn()) {
                try {
                    authentication.logIn();
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                }
            }
            if (isValid(authentication)) {
                ID = authentication.getSelectedProfile().getId();
                displayName = authentication.getSelectedProfile().getName();
                if (!authentication.isLoggedIn()) {
                    try {
                        authentication.logIn();
                    } catch (Exception e) {
                        if (e instanceof InvalidCredentialsException) {
                            Logger.logError("Invalid credentials recieved for user: " + authentication.getUserID() == null ? "" : authentication.getUserID());
                            Logger.logError(e.getMessage());
                            ErrorUtils.tossError("Invalid username or password.");
                            return null;
                        }
                        //offline mode??
                        if (e instanceof AuthenticationUnavailableException) {
                            Logger.logError(e.getMessage());
                        }
                        if (e instanceof UserMigratedException) {
                            Logger.logError(e.getMessage());
                            ErrorUtils.tossError("Invalid credentials, please make sure to login with your Mojang account.");
                        }
                        if (e instanceof AuthenticationException) {
                            Logger.logError("Unkown error from authlib:");
                            if (e.getMessage() == null) {
                                e.printStackTrace();
                            } else {
                                Logger.logError(e.getMessage());
                            }
                        }
                        if (e.getMessage() == null) {
                            Logger.logError("Unknown authentication error occurred");
                            e.printStackTrace();
                        } else {
                            Logger.logError(e.getMessage());
                        }
                        //e.printStackTrace();
                    }
                }
                if ((authentication.isLoggedIn()) && (authentication.canPlayOnline())) {
                    if ((authentication instanceof YggdrasilUserAuthentication)) {

                        return String.format("%s:token:%s:%s:%s",
                                new Object[] { authentication.getAgent().getVersion(), authentication.getAvailableProfiles()[0].getName(), authentication.getAuthenticatedToken(),
                                        authentication.getSelectedProfile().getId() });
                    }
                }
            }

        }
        return null;

    }

    private static boolean isValid (YggdrasilUserAuthentication authentication) {
        return ((authentication.isLoggedIn()) && (authentication.getAuthenticatedToken() != null) && (authentication.getSelectedProfile() != null));
    }

}
