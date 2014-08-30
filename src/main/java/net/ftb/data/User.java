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
package net.ftb.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.ftb.log.Logger;
import net.ftb.util.CryptoUtils;
import net.ftb.util.OSUtils;

public class User implements Serializable {
    /**
     * Increase serial if adding conversion ObjectInputStream
     */
    private static final int serialVersionUID = 1;

    private int _serial = 0;
    private boolean saveMojangData = true;
    private String _username = "", _encryptedPassword = "", _encryptedStore = "", _uuid = "";
    private transient String _password = "", _decryptedStore = "";

    /**
     * @param username - the username of the profile
     * @param password - the password of the profile
     */
    public User(String username, String password) {
        _serial = serialVersionUID;
        setUsername(username);
        setPassword(password);
    }

    /**
     * @param input - text with username, password, name, encrypted mojang datastore
     */
    @Deprecated
    public User(String input) {
        _serial = serialVersionUID;
        String[] tokens = input.split(":");
        setUsername(tokens[1]);
        if (tokens.length == 3) {
            setPassword(tokens[2]);
        } else if (tokens.length == 4) {
            setPassword(tokens[2]);
            setStore(tokens[3]);
        }
    }

    /**
     * @return - profile username
     */
    public String getUsername () {
        return _username;
    }

    /**
     * @param username - set profile username
     */
    public void setUsername (String username) {
        _username = username;
    }

    /**
     * @return - profile password
     */
    public String getPassword () {
        return _password;
    }

    /**
     * @return - authlib profile datastore
     */
    public String getDecryptedDatastore () {
        return _decryptedStore;
    }

    /**
     * @param store - set profile password
     */
    public void setStore (String store) {
        _decryptedStore = store;
        if (_decryptedStore == null || _decryptedStore.isEmpty()) {
            _encryptedStore = "";
        } else {
            _encryptedStore = CryptoUtils.encrypt(_decryptedStore);
        }
    }

    /**
     * @param password - set profile password
     */
    public void setPassword (String password) {
        _password = password;
        if (_password == null || _password.isEmpty()) {
            _encryptedPassword = "";
        } else {
            _encryptedPassword = CryptoUtils.encrypt(_password);
        }
    }

    private void readObject (ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        String password;
        switch(_serial) {
            case 0:
                //_serial not found by defaultReadObject()
                // TODO: legacy code remove later
                if (serialVersionUID == 1) {
                    Logger.logDebug("serialVersionUID == 1");
                    // convert old stored password to new format
                    if (!_encryptedPassword.isEmpty()) {
                        Logger.logInfo("Password is being converted to a newer format, ignore following decryption error");
                        Logger.logInfo("Converted password will be saved to disk after successful login");
                        password =  CryptoUtils.decrypt(_encryptedPassword);
                        _encryptedPassword =  CryptoUtils.encrypt(password);
                    }
                    _serial = 1;
                }
                break;
            case 1:
                if (!_encryptedPassword.isEmpty()) {
                    Logger.logInfo("Password is now encrypted with new key");
                    Logger.logInfo("Converted password will be saved to disk after successful login");
                    password =  CryptoUtils.decrypt(_encryptedPassword);
                    _encryptedPassword =  CryptoUtils.encrypt(password);
                }
                if (_encryptedStore != null && !_encryptedStore.isEmpty()) {
                    Logger.logInfo("mojang token is now encrypted with new key");
                    Logger.logInfo("Converted token will be saved to disk after successful login");
                    _decryptedStore =  CryptoUtils.decrypt(_encryptedStore);
                    _encryptedStore =  CryptoUtils.encrypt(_decryptedStore);
                }
                _serial = 2;
                break;
            default:
                break;
        }

        if (!_encryptedPassword.isEmpty()) {
            _password = CryptoUtils.decrypt(_encryptedPassword);
        } else {
            _password = "";
        }
        if (_encryptedStore != null && !_encryptedStore.isEmpty()) {
            _decryptedStore = CryptoUtils.decrypt(_encryptedStore);
        } else {
            _decryptedStore = null;
        }
    }

    private void writeObject (ObjectOutputStream s) {
        // clear mojangData if needed and then ...
        Logger.logDebug("starting...");
        if (!saveMojangData) {
            Logger.logDebug("Clearing mojangData");
            _encryptedStore = "";
        }
        try {
            s.defaultWriteObject();
        } catch (IOException e) {
            Logger.logError("logindata save failed", e);
        }
    }

    public void setUUID (String uuid) {
        this._uuid = uuid;
    }

    public String getUUID () {
        return this._uuid;
    }

    public void setSaveMojangData (boolean b) {
        saveMojangData = b;
    }

    public boolean getSaveMojangData () {
        return saveMojangData;
    }
}
