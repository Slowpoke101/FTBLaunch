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
        String displayName;
        if (user != null) {
            Logger.logInfo("Beginning authlib authentication attempt");
            YggdrasilUserAuthentication authentication = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
            Logger.logInfo("successfully created YggdrasilAuthenticationService");
            authentication.setUsername(user);
            authentication.setPassword(pass);
            if (!authentication.isLoggedIn()) {
                try {
                    try {
                        try {
                            try {
                                try {
                                    authentication.logIn();
                                } catch (UserMigratedException e) {
                                    Logger.logError(e.toString());
                                    ErrorUtils.tossError("Invalid credentials, please make sure to login with your Mojang account.");
                                    return null;
                                }
                            } catch (InvalidCredentialsException e) {

                                Logger.logError("Invalid credentials recieved for user: " + user);
                                Logger.logError(e.toString());
                                ErrorUtils.tossError("Invalid username or password.");
                                return null;

                            }
                        } catch (AuthenticationUnavailableException e) {
                            Logger.logError(e.toString());
                            return null;
                        }
                    } catch (AuthenticationException e) {
                        Logger.logError("Unkown error from authlib:");
                        if (e.getMessage() == null) {
                            e.printStackTrace();
                        } else {
                            Logger.logError(e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    if (e.getMessage() == null) {
                        Logger.logError("Unknown authentication error occurred");
                        e.printStackTrace();
                    } else {
                        Logger.logError(e.getMessage());
                    }
                    //e.printStackTrace();
                }
            }
            if (isValid(authentication)) {
                displayName = authentication.getSelectedProfile().getName();
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
