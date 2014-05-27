package net.ftb.workers;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.feed_the_beast.launcher.json.DateAdapter;
import net.feed_the_beast.launcher.json.EnumAdaptorFactory;
import net.feed_the_beast.launcher.json.FileAdapter;
import net.ftb.data.LoginResponse;
import net.ftb.data.UserManager;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.PasswordDialog;
import net.ftb.log.Logger;
import net.ftb.util.ErrorUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class AuthlibHelper {
    private static String uniqueID;

    protected static LoginResponse authenticateWithAuthlib (String user, String pass, String mojangData) {
        String displayName;
        boolean hasMojangData = false;
        boolean hasPassword = false;
        YggdrasilUserAuthentication authentication = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
        if (user != null) {
            Logger.logInfo("Beginning authlib authentication attempt");
            Logger.logInfo("successfully created YggdrasilAuthenticationService");
            authentication.setUsername(user);
            if (pass != null && !pass.isEmpty()) {
                authentication.setPassword(pass);
                hasPassword = true;
            }
            if (mojangData != null && !mojangData.isEmpty()) {
                Map<String, Object> m = decode(mojangData);
                if (m != null) {
                    authentication.loadFromStorage(m);
                    hasMojangData = true;
                }
            }
            if (authentication.canLogIn()) {
                try {
                    authentication.logIn();
                } catch (UserMigratedException e) {
                    Logger.logError(e.toString());
                    ErrorUtils.tossError("Invalid credentials, please make sure to login with your Mojang account.");
                    return null;
                } catch (InvalidCredentialsException e) {
                    Logger.logError("Invalid credentials recieved for user: " + user, e);
                    if (hasMojangData && hasPassword) {
                        uniqueID = authentication.getSelectedProfile().getId().toString();
                        //could be bad or expired keys, etc. will re-run w/o auth data to refresh and error after password was entered
                    } else {
                        ErrorUtils.tossError("Invalid username or password.");
                        return null;
                    }
                } catch (AuthenticationUnavailableException e) {
                    if (hasMojangData) {
                        //if the UUID is valid we can proceed to offline mode later
                        uniqueID = authentication.getSelectedProfile().getId().toString();
                        if (uniqueID != null && !uniqueID.isEmpty())
                            UserManager.setUUID(user, uniqueID);
                    }
                    ErrorUtils.tossError("Exception occurred, minecraft servers might be down. Check @ help.mojang.com", e);
                    if (uniqueID != null && !uniqueID.isEmpty()) {
                        UserManager.setUUID(user, uniqueID);
                        return new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token", user, null, uniqueID, authentication);
                    }
                    return null;
                } catch (AuthenticationException e) {
                    Logger.logError("Unkown error from authlib:");
                    if (e.getMessage() == null) {
                        Logger.logWarn("null", e);
                    } else {
                        Logger.logError(e.getMessage(), e);
                    }
                } catch (Exception e) {
                    if (e.getMessage() == null) {
                        Logger.logError("Unknown authentication error occurred", e);
                    } else {
                        Logger.logError(e.getMessage(), e);
                    }
                }
            }
            //Logger.logError("authDebug " + (hasMojangData ? "true" : "false") + " " + authentication.toString());
            if (isValid(authentication)) {
                displayName = authentication.getSelectedProfile().getName();
                if ((authentication.isLoggedIn()) && (authentication.canPlayOnline())) {
                    if ((authentication instanceof YggdrasilUserAuthentication)) {
                        UserManager.setStore(user, encode(authentication.saveForStorage()));
                        UserManager.setUUID(user, authentication.getSelectedProfile().getId().toString());//enables use of offline mode later if needed on newer MC Versions
                        return new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token", displayName, authentication.getAuthenticatedToken(), authentication
                                .getSelectedProfile().getId().toString(), authentication);
                    }
                }
            }

        }
        if (hasMojangData) {
            Logger.logError("Failed to authenticate with mojang data, attempting to use username & password");
            if (!hasPassword) {
                new PasswordDialog(LaunchFrame.getInstance(), true).setVisible(true);
                if (LaunchFrame.tempPass.isEmpty())
                    return null;
                pass = LaunchFrame.tempPass;
            }

            LoginResponse l = authenticateWithAuthlib(user, pass, null);
            if (l == null) {
                Logger.logError("Failed to login with username & password");
                return l;
            } else {
                return l;
            }
        }
        return null;

    }

    private static boolean isValid (YggdrasilUserAuthentication authentication) {
        return ((authentication.isLoggedIn()) && (authentication.getAuthenticatedToken() != null) && (authentication.getSelectedProfile() != null));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> decode (String s) {
        try {
            Map<String, Object> ret;
            JsonObject jso = new JsonParser().parse(s).getAsJsonObject();
            ret = (Map<String, Object>) decodeElement(jso);
            return ret;
        } catch (Exception e) {
            Logger.logError("Error decoding Authlib JSON", e);
            return null;
        }
    }

    private static Object decodeElement (JsonElement e) {
        if (e instanceof JsonObject) {
            Map<String, Object> ret = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, JsonElement> jse : ((JsonObject) e).entrySet()) {
                ret.put(jse.getKey(), decodeElement(jse.getValue()));
            }
            return ret;
        }
        if (e instanceof JsonArray) {
            List<Object> ret = new ArrayList<Object>();
            for (JsonElement jse : e.getAsJsonArray()) {
                ret.add(decodeElement(jse));
            }
            return ret;

        }
        return e.getAsString();
    }

    private static String encode (Map<String, Object> m) {
        try {
            Gson gson;
            final GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapterFactory(new EnumAdaptorFactory());
            builder.registerTypeAdapter(Date.class, new DateAdapter());
            builder.registerTypeAdapter(File.class, new FileAdapter());
            builder.enableComplexMapKeySerialization();
            builder.setPrettyPrinting();
            gson = builder.create();
            return gson.toJson(m);
        } catch (Exception e) {
            Logger.logError("Error encoding Authlib JSON", e);
            return null;
        }

    }

}
