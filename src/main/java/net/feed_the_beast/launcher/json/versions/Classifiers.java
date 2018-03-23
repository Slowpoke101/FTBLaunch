/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
