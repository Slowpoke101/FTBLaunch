/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.tracking.google;

import java.security.SecureRandom;

public class VisitorData {
    private int visitorId;
    private long timestampFirst;
    private long timestampPrevious;
    private long timestampCurrent;
    private int visits;

    VisitorData (int visitorId, long timestampFirst, long timestampPrevious, long timestampCurrent, int visits) {
        this.visitorId = visitorId;
        this.timestampFirst = timestampFirst;
        this.timestampPrevious = timestampPrevious;
        this.timestampCurrent = timestampCurrent;
        this.visits = visits;
    }

    public void resetSession () {
        long now = now();
        this.timestampPrevious = this.timestampCurrent;
        this.timestampCurrent = now;
        this.visits++;
    }

    private static long now () {
        return System.currentTimeMillis() / 1000L;
    }

    public int getVisitorId () {
        return visitorId;
    }

    public long getTimestampFirst () {
        return timestampFirst;
    }

    public long getTimestampPrevious () {
        return timestampPrevious;
    }

    public long getTimestampCurrent () {
        return timestampCurrent;
    }

    public int getVisits () {
        return visits;
    }

    /**
     * initializes a new visitor data, with new visitorid
     */
    public static VisitorData newVisitor () {
        int visitorId = (new SecureRandom().nextInt() & 0x7FFFFFFF);
        long now = now();
        return new VisitorData(visitorId, now, now, now, 1);
    }

    public static VisitorData newSession (int visitorId, long timestampfirst, long timestamplast, int visits) {
        long now = now();
        return new VisitorData(visitorId, timestampfirst, timestamplast, now, visits + 1);
    }
}