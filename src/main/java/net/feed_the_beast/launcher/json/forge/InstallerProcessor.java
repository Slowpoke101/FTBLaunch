package net.feed_the_beast.launcher.json.forge;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InstallerProcessor {
    private String jar;
    private List<String> classpath;
    private List<String> args;
    private Map<String, String> outputs;
}
