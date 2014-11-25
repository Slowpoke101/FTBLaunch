package net.ftb.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.ftb.data.ModPack;
import net.ftb.log.Logger;

import com.beust.jcommander.internal.Sets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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

    private ModPackUtil() {
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
            try {
                FileSystem system = FileSystems.newFileSystem(modPackZip.toPath(), null);

                ModVisitor visitor = new ModVisitor();
                Files.walkFileTree(system.getPath(File.separator), visitor);
                return visitor.getFileNames();
            } catch (IOException e) {
                Logger.logError("Error attempting to read default mods", e);
                return Sets.newHashSet();
            }
        }

        return Sets.newHashSet();
    }

    /**
     * File visitor which can traverse the tree of the a file system looking for mod files
     */
    private static final class ModVisitor implements FileVisitor<Path> {

        /** Discovered mod files */
        private Set<String> fileNames = new HashSet<String>();

        @Override
        public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) throws IOException {
            //Attempted to limit the searched directories to the "mods", "coremods", and "instMods" directories 
            //for efficiency, however for some reason it just skipped all the directories instead. Something to 
            //look into later. Original attempt: 
            // if (dir.endsWith("minecraft" + File.separator + "mods") 
            // || dir.endsWith("minecraft" + File.separator + "coremods") 
            // || dir.endsWith("instMods")) 

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
            String fileName = file.getFileName().toString();

            if (isMod(fileName)) {
                fileNames.add(fileName.toLowerCase());
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed (Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory (Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        public Set<String> getFileNames () {
            return fileNames;
        }

        /**
         * Determines if the given file is a mod file
         * @param name The name of the file
         * @return True if the file is a mod file, false otherwise
         */
        private boolean isMod (String name) {
            //Based on the filtering in the edit mod pack dialog - it limits the available entries in this manner
            return name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".litemod") || name.toLowerCase().endsWith(".zip.disabled")
                    || name.toLowerCase().endsWith(".jar.disabled") || name.toLowerCase().endsWith(".litemod.disabled");
        }
    }
}
