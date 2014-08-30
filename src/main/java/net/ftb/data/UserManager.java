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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.ProfileAdderDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.CryptoUtils;
import net.ftb.util.OSUtils;

public class UserManager {
    public final static ArrayList<User> _users = new ArrayList<User>();

    public UserManager() {
    }

    public void write () throws IOException {
        //we don't want this info stored at all!
    }

    public void read () {
            ProfileAdderDialog p = new ProfileAdderDialog(LaunchFrame.getInstance(), true);
            p.setVisible(true);
    }

    public static void addUser (String username, String password) {
        _users.add(new User(username, password));
    }

    public static ArrayList<String> getUsernames () {
        ArrayList<String> ret = new ArrayList<String>();
        for (User user : _users) {
            ret.add(user.getUsername());
        }
        return ret;
    }



    public static String getUsername (String name) {
        for (User user : _users) {
            if (user.getUsername().equals(name)) {
                return user.getUsername();
            }
        }
        return "";
    }

    public static String getPassword (String name) {
        for (User user : _users) {
            if (user.getUsername().equals(name)) {
                return user.getPassword();
            }
        }
        return "";
    }

    private static User findUser (String name) {
        for (User user : _users) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public static void removePass (String username) {
        for (User user : _users) {
            if (user.getUsername().equals(username)) {
                user.setPassword("");
                return;
            }
        }
    }

    public static void removeUser (String name) {
        User temp = findUser(name);
        if (temp != null) {
            _users.remove(_users.indexOf(temp));
        }
    }

    public static void updateUser (String oldName, String username, String password, String name) {
        User temp = findUser(oldName);
        if (temp != null) {
            _users.get(_users.indexOf(temp)).setUsername(username);
            _users.get(_users.indexOf(temp)).setPassword(password);
        }
    }

    //used by authlib helper in order to send key's back to disc for next load
    public static void setStore (String user, String encode) {
        if (encode != null && !encode.isEmpty()){
            User temp = findUser(user);
            if(temp != null)
                temp.setStore(encode);
        }
    }

    //used by authlib helper in order to send key's back to disc for next load
    public static String getMojangData (String user) {
        User temp = findUser(user);
        if(temp != null)
            return temp.getDecryptedDatastore();
        return null;
    }


    public static String getUUID (String username) {
        User temp = findUser(username);
        if(temp != null)
            return temp.getDecryptedDatastore();
        return null;
    }

    public static void setUUID (String username, String uuid) {
        User temp = findUser(username);
        if(temp != null)
            temp.setUUID(uuid);
    }

    public static void setSaveMojangData (String username, boolean b) {
        User temp = findUser(username);
        if(temp != null)
            temp.setSaveMojangData(b);
    }

    public static boolean getSaveMojangData (String username) {
        User temp = findUser(username);
        return temp != null && temp.getSaveMojangData();
    }
}
