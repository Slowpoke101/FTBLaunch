package net.ftb.util;

import com.beust.jcommander.internal.Sets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.ftb.data.ModPack;
import net.ftb.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;

/**
 * Utility functions specific to mod packs, such as information on default mods
 */
public final class ModPackUtil {

    /** Cached results for lookups into the zip archives for the mod packs - avoid repeated file I/O*/
    private static Cache<String, Set<String>> defaultMods;

    static {
        //The "10" initial size is somewhat arbitrary - may be changed without significant concern
        defaultMods = CacheBuilder.newBuilder().initialCapacity(10).build();
    }

    private ModPackUtil () {
        //Prevent instantiation of the utility class, meant to provide static methods
    }

    /**
     * Looks up the set of mod files which are downloaded with a "stock" installation of the mod 
     * pack, before any user customization
     *
     * <p>
     * Uses Java FileSystem to read the mods files from the cached/downloaded zip archive of the mod, 
     * and caches the result to avoid repeated file I/O
     * </p>
     *
     * @param modpack The mod pack to look up default mod entries for
     * @return A set of the mod files found in the default mod archive, or an empty set if the 
     * archive could not be located
     */
    public static Set<String> getDefaultModFiles (ModPack modpack) {
        //DEV NOTE: A set is used to ensure that there are no duplicate entries in the returned group, and to 
        //utilize the more efficient backing implementation for lookup operations such as .contains()
        if (modpack != null) {
            String modpackKey = getModpackCacheKey(modpack);
            Set<String> defaults = defaultMods.getIfPresent(modpackKey);

            if (defaults == null || defaults.isEmpty()) {
                defaults = loadDefaultMods(modpack);
                defaultMods.put(modpackKey, defaults);
            }

            return defaults;
        } else {
            Logger.logWarn("Null modpack provided for retrieving default mod data");
        }

        return Sets.newHashSet();
    }

    /**
     * Clears the cached entry for default mods in a mod pack. Used when the mod pack is 
     * being updated or forcibly reloaded
     * @param modpack The mod pack to clear cached data for
     */
    public static void clearDefaultModFiles (ModPack modpack) {
        if (modpack != null) {
            defaultMods.invalidate(getModpackCacheKey(modpack));
        } else {
            Logger.logWarn("Null modpack provided for clearing default mod data");
        }
    }

    /**
     * @param modpack The modpack to construct a lookup key for
     * @return A unique string key used to reference the modpack's data in the cache
     */
    private static String getModpackCacheKey (@Nonnull ModPack modpack) {
        //Using the directory combined with the URL, because this is used as the cached download location for each mod,
        //and should therefore be guaranteed unique per modpack
        return (modpack.getDir() + ":" + modpack.getUrl());
    }

    /**
     * Performs file system lookup and reading of the default mods in the archive
     * @param modpack The mod pack to look up
     * @return A set of the mod files found in the default mod archive, or an empty set if the 
     * archive could not be located
     */
    private static Set<String> loadDefaultMods (@Nonnull ModPack modpack) {
        //This was written based on ModManager's update routine
        String installPath = OSUtils.getCacheStorageLocation();
        File modPackZip = new File(installPath, "ModPacks" + File.separator + modpack.getDir() + File.separator + modpack.getUrl());

        if (modPackZip.exists()) {
            ZipInputStream zip = null;

            try {
                zip = new ZipInputStream(new FileInputStream(modPackZip));

                Set<String> fileNames = new HashSet<String>();
                ZipEntry ze;

                while ((ze = zip.getNextEntry()) != null) {
                    if (!ze.isDirectory()) {
                        String fileName = ze.getName();

                        if (fileName != null) {
                            //This is always '/', regardless of the OS - does not match the value of File.separator
                            int lastSeparator = fileName.lastIndexOf('/');
                            if (fileName.length() > lastSeparator + 1) {
                                if (lastSeparator != -1) {
                                    fileName = fileName.substring(lastSeparator + 1);
                                }

                                fileNames.add(fileName.toLowerCase());
                            }
                        }
                    }
                }

                return fileNames;

                //TODO (romeara) - When the launcher is upgraded to Java7, switch to using nio's FileSystem and visitor pattern, 
                // it is significantly more performant, such as below:
                // FileSystem system = FileSystems.newFileSystem(modPackZip.toPath(), null);
                //                
                // Visitor is an implements of FileVistor
                // Files.walkFileTree(system.getPath(File.separator), visitor);
                // return visitor.getFileNames();
            } catch (IOException e) {
                Logger.logError("Error attempting to read default mods", e);
                return Sets.newHashSet();
            } finally {
                if (zip != null) {
                    try {
                        zip.close();
                    } catch (IOException e) {
                        Logger.logError("Error attempting to close stream used to read default mods", e);
                    }
                }
            }
        }

        return Sets.newHashSet();
    }
}
