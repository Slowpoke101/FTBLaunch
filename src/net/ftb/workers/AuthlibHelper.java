package net.ftb.workers;

import java.net.Proxy;

import net.ftb.log.Logger;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
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
                }
                catch (AuthenticationException e) {
                    e.printStackTrace();
                }
            }
            if (isValid(authentication)) {
                ID = authentication.getSelectedProfile().getId();
                displayName = authentication.getSelectedProfile().getName();
                if (!authentication.isLoggedIn()) {
                    try {
                        authentication.logIn();
                    }
                    catch (AuthenticationException e) {
                        e.printStackTrace();
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
