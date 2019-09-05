package net.feed_the_beast.launcher.json.forge;

import lombok.Data;

import java.util.List;

@Data
public class InstallerProcessor {
    private String jar;
    private List<String> classpath;
    private List<String> args;

/*
            "outputs": {
                "{PATCHED}": "{PATCHED_SHA}"
            }

 */
}
