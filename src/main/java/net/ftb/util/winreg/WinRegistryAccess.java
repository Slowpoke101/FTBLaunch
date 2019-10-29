package net.ftb.util.winreg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.prefs.Preferences;

public abstract class WinRegistryAccess {

    private static WinRegistryAccess registryAccess = null;

    public static WinRegistryAccess getInstance() {
        if(registryAccess == null) {
            if(JavaVersion.createJavaVersion(System.getProperty("java.version")).isOlder("11")){
                registryAccess = new WinRegistryLegacy();
            } else {
                registryAccess = new WinRegistry();
            }
        }
        return registryAccess;
    }

    protected final int HKEY_CURRENT_USER = 0x80000001;
    public final int HKEY_LOCAL_MACHINE = 0x80000002;
    protected final int REG_SUCCESS = 0;

    public final int KEY_WOW64_32KEY = 0x0200;
    public final int KEY_WOW64_64KEY = 0x0100;

    protected final int KEY_READ = 0x20019;

    final Preferences userRoot = Preferences.userRoot();
    final Preferences systemRoot = Preferences.systemRoot();
    final Class<? extends Preferences> userClass = userRoot.getClass();
    Method regOpenKey = null;
    Method regCloseKey = null;
    Method regQueryValueEx = null;
    Method regEnumValue = null;
    Method regQueryInfoKey = null;
    Method regEnumKeyEx = null;
    Method regCreateKeyEx = null;
    Method regSetValueEx = null;
    Method regDeleteKey = null;
    Method regDeleteValue = null;

    /**
     * Read a value from key and value name
     * @param hkey   HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key registry key
     * @param valueName registry value
     * @param wow64  0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
     *               or KEY_WOW64_32KEY to force access to 32-bit registry view,
     *               or KEY_WOW64_64KEY to force access to 64-bit registry view
     * @return the value
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    abstract String readString (int hkey, String key, String valueName, int wow64) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

    /**
     * Read the value name(s) from a given key
     * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key registry key
     * @param wow64  0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
     *               or KEY_WOW64_32KEY to force access to 32-bit registry view,
     *               or KEY_WOW64_64KEY to force access to 64-bit registry view
     * @return the value name(s)
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    abstract List<String> readStringSubKeys (int hkey, String key, int wow64) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
}
