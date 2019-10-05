package net.feed_the_beast.launcher.json.forge;

import lombok.Data;
import net.feed_the_beast.launcher.json.versions.Library;

import java.util.List;
import java.util.Map;

@Data
public class InstallProfile {
    private String _comment_;
    private int spec;
    private String version;
    private String json;
    private String path;
    private String logo;
    private String minecraft;
    private String welcome;
    private Map<String, InstallerData> data;
    private List<InstallerProcessor> processors;
    private List<Library> libraries;
    private Library installerjar;

    /*
        "spec": 0,
    "profile": "forge",
    "version": "1.14.4-forge-28.0.11",
    "json": "/version.json",
    "path": "net.minecraftforge:forge:1.14.4-28.0.11",
    "logo": "/big_logo.png",
    "minecraft": "1.14.4",
    "welcome": "Welcome to the simple Forge installer.",

     */
}
