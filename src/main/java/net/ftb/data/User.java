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
    private String _username = "", _name = "", _encryptedPassword = "", _encryptedStore = "", _uuid = "";
    private transient String _password = "", _decryptedStore = "";

    /**
     * @param username - the username of the profile
     * @param password - the password of the profile
     * @param name - the name of the profile
     */
    public User(String username, String password, String name) {
        _serial = serialVersionUID;
        setUsername(username);
        setPassword(password);
        setName(name);
    }

    /**
     * @param input - text with username, password, name, encrypted mojang datastore
     */
    @Deprecated
    public User(String input) {
        _serial = serialVersionUID;
        String[] tokens = input.split(":");
        setName(tokens[0]);
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
            _encryptedStore = CryptoUtils.encrypt(_decryptedStore, OSUtils.getMacAddress());
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
            _encryptedPassword = CryptoUtils.encrypt(_password, OSUtils.getMacAddress());
        }
    }

    /**
     * @return - profile name
     */
    public String getName () {
        return _name;
    }

    /**
     * @param name - set profile name
     */
    public void setName (String name) {
        _name = name;
    }

    private void readObject (ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        switch(_serial) {
            case 0:
                //_serial not found by defaultReadObject()
                if (serialVersionUID == 1) {
                    // convert old stored password to new format
                    if (!_encryptedPassword.isEmpty()) {
                        Logger.logInfo("Password is being converted to a newer format, ignore following decryption error");
                        Logger.logInfo("Converted password will be saved to disk after successful login");
                        String password =  CryptoUtils.decrypt(_encryptedPassword, OSUtils.getMacAddress());
                        _encryptedPassword =  CryptoUtils.encrypt(password, OSUtils.getMacAddress());
                    }
                    _serial = 1;
                }
                break;
            default:
                break;
        }

        if (!_encryptedPassword.isEmpty()) {
            _password = CryptoUtils.decrypt(_encryptedPassword, OSUtils.getMacAddress());
        } else {
            _password = "";
        }
        if (_encryptedStore != null && !_encryptedStore.isEmpty()) {
            _decryptedStore = CryptoUtils.decrypt(_encryptedStore, OSUtils.getMacAddress());
        } else {
            _decryptedStore = null;
        }
    }

    public void setUUID (String uuid) {
        this._uuid = uuid;
    }

    public String getUUID () {
        return this._uuid;
    }
}
