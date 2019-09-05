package net.feed_the_beast.launcher.json.forge;

import lombok.Data;

@Data
public class InstallerData {
    private InstallerDataItem MAPPINGS;
    private InstallerDataItem BINPATCH;
    private InstallerDataItem MC_SLIM;
    private InstallerDataItem MC_SLIM_SHA;
    private InstallerDataItem MC_EXTRA;
    private InstallerDataItem MC_EXTRA_SHA;
    private InstallerDataItem MC_SRG;
    private InstallerDataItem PATCHED;
    private InstallerDataItem PATCHED_SHA;
    private InstallerDataItem MCP_VERSION;
}
