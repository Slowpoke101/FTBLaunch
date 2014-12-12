package net.feed_the_beast.launcher.json.java;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * @author progwml6
 */
public class JavaOS {

    @Getter
    @SerializedName("32")
    private JavaType bits32;
    @Getter
    @SerializedName("64")
    private JavaType bits64;

}
