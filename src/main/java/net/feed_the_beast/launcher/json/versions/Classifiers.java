package net.feed_the_beast.launcher.json.versions;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class Classifiers {
    @SerializedName("natives-linux")
    public Artifact nativeslinux;
    @SerializedName("natives-windows")
    public Artifact nativeswindows32;
    @SerializedName("natives-windows-32")
    public Artifact nativeswindows64;
    @SerializedName("natives-windows-64")
    public Artifact nativeswindows;
    @SerializedName("natives-osx")
    public Artifact nativesosx;

    @Nullable
    public Artifact getArtifactForOS(){
        switch (OS.CURRENT) {
        case OSX:
            return nativesosx;
        case LINUX:
            return nativeslinux;
        case WINDOWS:
            return nativeswindows;
        default:
            return null;
        }



    }
}
