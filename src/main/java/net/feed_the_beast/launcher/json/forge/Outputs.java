package net.feed_the_beast.launcher.json.forge;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Outputs {
    //
    @SerializedName("{PATCHED}")
    private String patched;
    // "{MC_SLIM}": "{MC_SLIM_SHA}",
    @SerializedName("{MC_SLIM}")
    private String mcslim;
    // "{MC_EXTRA}": "{MC_EXTRA_SHA}"
    @SerializedName("{MC_EXTRA}")
    private String mcextra;
}
