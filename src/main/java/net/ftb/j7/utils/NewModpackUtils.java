package net.ftb.j7.utils;

import com.google.common.collect.Sets;
import net.ftb.data.ModPack;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

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

/**
 * @author romeara
 * adapted from PR to this class by progwml6
 */
public class NewModpackUtils {
    private NewModpackUtils () {

    }

    /**
     * Performs file system lookup and reading of the default mods in the archive
     * @param modpack The mod pack to look up
     * @return A set of the mod files found in the default mod archive, or an empty set if the
     * archive could not be located
     */
    public static Set<String> loadDefaultMods (@Nonnull ModPack modpack) {
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
