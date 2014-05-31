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
    private File _oldFile;

    public UserManager(File file, File oldFile) {
        _file = file;
        _oldFile = oldFile;
        read();
    }

    public void write () throws IOException {

        if (OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS) {
                if (_oldFile.exists()) {
                    _oldFile.delete();
                }

                if (_file.exists()) {
                    _file.delete();
                }
        }

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
        if (!_file.exists() && !_oldFile.exists()) {
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
            FileInputStream fileInputStream;
            
            if(_file.exists()) {
                fileInputStream = new FileInputStream(_file);
            } else {
                fileInputStream = new FileInputStream(_oldFile);
            }
            
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
                BufferedReader read;
                
                if(_file.exists()) {
                    read = new BufferedReader(new FileReader(_file));
                } else {
                    read = new BufferedReader(new FileReader(_oldFile));
                }
                
                String str;
                while ((str = read.readLine()) != null) {
                    str = CryptoUtils.decryptLegacy(str, OSUtils.getMacAddress());
                    _users.add(new User(str));
                }
                read.close();
            } catch (NumberFormatException ex) {
                // If logindata is new format and empty it will contain bytes 0xae 0xed 0x00 0x05
                // Catch exception from parseInt => no more stack prints for end users
            } catch (Exception ex) {
                Logger.logError("Error while reading logindata", ex);
            }
            if (_users.isEmpty()) {
                Logger.logInfo("No users found after decoding old logindata format. Malformed logindata or empty logindata");
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

}
