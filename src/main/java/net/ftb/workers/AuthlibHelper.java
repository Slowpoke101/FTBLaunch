/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.workers;

import java.io.File;
import java.net.Proxy;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.feed_the_beast.launcher.json.DateAdapter;
import net.feed_the_beast.launcher.json.EnumAdaptorFactory;
import net.feed_the_beast.launcher.json.FileAdapter;
import net.ftb.data.LoginResponse;
import net.ftb.data.UserManager;
import net.ftb.gui.LauncherFrame;
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

    protected static LoginResponse authenticateWithAuthlib (String user, String pass, String mojangData, String selectedProfileName) {
        String displayName;
        boolean hasMojangData = false;
        boolean hasPassword = false;
        GameProfile selectedProfile = null;
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
                Logger.logDebug("mojangData was passed to current method");
                Map<String, Object> m = decode(mojangData);
                if (m != null) {
                    Logger.logDebug("Loading mojangData into authlib");
                    authentication.loadFromStorage(m);
                    hasMojangData = true;
                }
            } else {
                Logger.logDebug("mojangData is null or empty");
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
                    Logger.logDebug("Error while authenticating", e);
                    if (hasMojangData) {
                        //if the UUID is valid we can proceed to offline mode later
                        uniqueID = authentication.getSelectedProfile().getId().toString();
                        if (uniqueID != null && !uniqueID.isEmpty())
                            Logger.logDebug("Setting UUID");
                            UserManager.setUUID(user, uniqueID);
                    }
                    if (uniqueID != null && !uniqueID.isEmpty()) {
                        UserManager.setUUID(user, uniqueID);
                        Logger.logDebug("Setting UUID and creating and returning new LoginResponse");
                        return new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token", user, null, uniqueID, authentication);
                    }
                    ErrorUtils.tossError("Exception occurred, minecraft servers might be down. Check @ help.mojang.com", e);
                    return null;
                } catch (AuthenticationException e) {
                    Logger.logError("Unkown error from authlib:", e);
                } catch (Exception e) {
                    Logger.logError("Unknown authentication error occurred", e);
                }
            } else {
                Logger.logDebug("authentication.canLogIn() returned false");
            }

            if (isValid(authentication)) {
                Logger.logDebug("Authentication is valid ");
                displayName = authentication.getSelectedProfile().getName();
                if ((authentication.isLoggedIn()) && (authentication.canPlayOnline())) {
                    Logger.logDebug("loggedIn() && CanPlayOnline()");
                    if ((authentication instanceof YggdrasilUserAuthentication)) {
                        UserManager.setStore(user, encode(authentication.saveForStorage()));
                        UserManager.setUUID(user, authentication.getSelectedProfile().getId().toString());//enables use of offline mode later if needed on newer MC Versions
                        Logger.logDebug("Authentication done, returning LoginResponse");
                        return new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token", displayName, authentication.getAuthenticatedToken(), authentication
                                .getSelectedProfile().getId().toString(), authentication);
                    }
                }
                Logger.logDebug("this should never happen: isLoggedIn: " + authentication.isLoggedIn() + " canPlayOnline(): " + authentication.canPlayOnline());
            } else if (authentication.getSelectedProfile() == null && (authentication.getAvailableProfiles() != null && authentication.getAvailableProfiles().length != 0 )) {
                // user has more than one profile
                Logger.logDebug("User has more than one profile");
                for (GameProfile profile : authentication.getAvailableProfiles()) {
                    if (selectedProfileName.equals(profile.getName())) {
                        Logger.logDebug("profile found");
                        selectedProfile = profile;
                    }
                }
                if (selectedProfile == null) {
                    Logger.logDebug("profile not found, defaulting to first");
                    selectedProfile = authentication.getAvailableProfiles()[0];
                }
                Logger.logDebug("Authentication done, returning LoginResponse");
                return new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token", selectedProfile.getName(), authentication.getAuthenticatedToken(),
                        selectedProfile.getId().toString(), authentication);
            } else if (authentication.getSelectedProfile() == null && (authentication.getAvailableProfiles() != null && authentication.getAvailableProfiles().length == 0 )) {
                ErrorUtils.showClickableMessage("You need to own minecraft to play FTB Modpacks", "https://help.mojang.com/customer/portal/articles/1218766-can-only-play-minecraft-demo");
                return null;
            } else {
                Logger.logDebug("this should never happen");
            }

        } else {
            Logger.logDebug("this should never happen");
        }

        if (hasMojangData) {
            Logger.logError("Failed to authenticate with mojang data, attempting to use username & password");
            if (!hasPassword) {
                new PasswordDialog(LauncherFrame.getInstance(), true).setVisible(true);
                if (LauncherFrame.tempPass.isEmpty())
                    return null;
                pass = LauncherFrame.tempPass;
            }

            LoginResponse l = authenticateWithAuthlib(user, pass, null, selectedProfileName);
            if (l == null) {
                Logger.logError("Failed to login with username & password");
                return null;
            } else {
                Logger.logDebug("authentication ready, returning LoginResponse from authlib");
                return l;
            }
        }
        Logger.logError("Failed to authenticate");
        return null;

    }

    private static boolean isValid (YggdrasilUserAuthentication authentication) {
        boolean ret = true;
        if (!authentication.isLoggedIn()) {
            Logger.logDebug("authentication not valid");
            ret = false;
        }
        if (authentication.getAuthenticatedToken() == null) {
            Logger.logDebug("authentication not valid");
            ret = false;
        }
        if (authentication.getSelectedProfile() == null) {
            Logger.logDebug("authentication not valid");
            ret = false;
        }
        return ret;
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
            Map<String, Object> ret = Maps.newLinkedHashMap();
            for (Map.Entry<String, JsonElement> jse : ((JsonObject) e).entrySet()) {
                ret.put(jse.getKey(), decodeElement(jse.getValue()));
            }
            return ret;
        }
        if (e instanceof JsonArray) {
            List<Object> ret = Lists.newArrayList();
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
