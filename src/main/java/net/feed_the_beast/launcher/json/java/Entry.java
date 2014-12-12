package net.feed_the_beast.launcher.json.java;

import lombok.Getter;

/**
 * @author progwml6
 */
public class Entry {

    @Getter
    /**
     * returns the sha1 of the url
     */
    private String sha1;
    @Getter
    /**
     * returns the url of the file to download
     */
    private String url;
    @Getter
    /**
     * returns java version ex: 1.8.0_25
     */
    private String version;
}
