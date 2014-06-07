package net.feed_the_beast.launcher.json.launcher;

import lombok.Data;
import net.feed_the_beast.launcher.json.versions.Library;

import java.util.List;

@Data
public class Channel {
    private Library channelUpdate;
    private Library file;
    private int version;
    private int jenkins;
    private String changelog;
    private String note;
}
