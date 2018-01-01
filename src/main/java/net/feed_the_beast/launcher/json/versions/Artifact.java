package net.feed_the_beast.launcher.json.versions;

import lombok.Getter;
import lombok.Setter;

import java.net.URL;
@Getter
@Setter
public class Artifact {
    private String path;
    private URL url;
    private String sha1;
    private int size;

}
