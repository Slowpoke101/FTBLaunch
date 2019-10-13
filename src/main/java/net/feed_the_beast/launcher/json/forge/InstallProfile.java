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
}
