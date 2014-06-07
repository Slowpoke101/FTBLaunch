package net.feed_the_beast.launcher.json.launcher;

import lombok.Data;

@Data
public class Update {
    private Channel beta;
    private Channel release;
    private String primary;
}
