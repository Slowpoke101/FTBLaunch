/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
    private File _file;

    public UserManager(File file) {
        _file = file;
        read();
    }

    public void write () throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(_file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        try {
            for (User user : _users) {
                objectOutputStream.writeObject(user);
            }
        } finally {
            objectOutputStream.close();
            fileOutputStream.close();
        }
    }

    public void read () {
        if (!_file.exists()) {
            return;
        }
        _users.clear();
        if (!OSUtils.verifyUUID()) {
            Logger.logError(I18N.getLocaleString("CHANGEDUUID"));
            ProfileAdderDialog p = new ProfileAdderDialog(LaunchFrame.getInstance(), "CHANGEDUUID", true);
            p.setVisible(true);
            return;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(_file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            try {
                Object obj;
                while ((obj = objectInputStream.readObject()) != null) {
                    if (obj instanceof User) {
                        _users.add((User) obj);
                    }
                }
            } catch (EOFException ignored) {
            } finally {
                objectInputStream.close();
                fileInputStream.close();
            }
        } catch (StreamCorruptedException e) {
            Logger.logWarn("Failed to decode logindata. Trying old format");
        } catch (Exception e) {
            Logger.logError("Failed to decode logindata", e);
        }

        // TODO: Remove this in a while once people are unlikely to have old format saved logindata
        if (_users.isEmpty()) {
            //Logger.logError(I18N.getLocaleString("OLDCREDS"));
            // ProfileAdderDialog p = new ProfileAdderDialog(LaunchFrame.getInstance(), "OLDCREDS", true);
            // p.setVisible(true);

            try {
                BufferedReader read = new BufferedReader(new FileReader(_file));
                String str;
                while ((str = read.readLine()) != null) {
                    str = CryptoUtils.decrypt(str, OSUtils.getMacAddress());
                    _users.add(new User(str));
                }
                read.close();
            } catch (NumberFormatException ex) {
                // If logindata is new format and empty it will contain bytes 0xae 0xed 0x00 0x05
                // Catch exception from parseInt => no more stack prints for end users
            } catch (Exception ex) {
                Logger.logError(ex.getMessage(), ex);
            }
        }
    }

    public static void addUser (String username, String password, String name) {
        _users.add(new User(username, password, name));
    }

    public static ArrayList<String> getUsernames () {
        ArrayList<String> ret = new ArrayList<String>();
        for (User user : _users) {
            ret.add(user.getName());
        }
        return ret;
    }

    public static ArrayList<String> getNames () {
        ArrayList<String> ret = new ArrayList<String>();
        for (User user : _users) {
            ret.add(user.getName());
        }
        return ret;
    }

    public static String getUsername (String name) {
        for (User user : _users) {
            if (user.getName().equals(name)) {
                return user.getUsername();
            }
        }
        return "";
    }

    public static String getPassword (String name) {
        for (User user : _users) {
            if (user.getName().equals(name)) {
                return user.getPassword();
            }
        }
        return "";
    }

    private static User findUser (String name) {
        for (User user : _users) {
            if (user.getName().equals(name)) {
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
            _users.get(_users.indexOf(temp)).setName(name);
        }
    }
}
