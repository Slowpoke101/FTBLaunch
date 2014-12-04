package net.ftb.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.junit.Test;
import static org.junit.Assert.assertTrue;

@SuppressWarnings( "unchecked" )
public class ComparableMcVersionTest {
    private Comparable newComparable (String version) {
        return new ComparableVersion(version);
    }

    /*
     * Snapshots with <version>-preX naming scheme can be sorted correctly
     * Weekly snapshots with XXwYYZ scheme will be detected newer then normal MC versions
     * Older snapshot versioning schemes IDK. Really  do not even care
     *
     * How to implement correct comparisong for Mc snapshots:
     * Read releaseTimes from  http://s3.amazonaws.com/Minecraft.Download/versions/versions.json
     *  - e.g. is 14w25b snapshot of  1.7.10 or 1.8?
     */
    private static final String[] VERSIONS =
            {"1.5.2", "1.6.2", "1.6.4", "1.7.2", "1.7.10-pre2", "1.7.10", "1.8-pre1", "1.8-pre2", "1.8", "1.8.1", "1.9-pre1", "14w04b", "14w33a"};

    private void checkVersionsOrder (String[] versions) {
        Comparable[] c = new Comparable[versions.length];
        for (int i = 0; i < versions.length; i++) {
            c[i] = newComparable(versions[i]);
        }

        for (int i = 1; i < versions.length; i++) {
            Comparable low = c[i - 1];
            for (int j = i; j < versions.length; j++) {
                Comparable high = c[j];
                assertTrue("expected " + low + " < " + high, low.compareTo(high) < 0);
                assertTrue("expected " + high + " > " + low, high.compareTo(low) > 0);
            }
        }
    }

    private void checkVersionsEqual (String v1, String v2) {
        Comparable c1 = newComparable(v1);
        Comparable c2 = newComparable(v2);
        assertTrue("expected " + v1 + " == " + v2, c1.compareTo(c2) == 0);
        assertTrue("expected " + v2 + " == " + v1, c2.compareTo(c1) == 0);
        assertTrue("expected same hashcode for " + v1 + " and " + v2, c1.hashCode() == c2.hashCode());
        assertTrue("expected " + v1 + ".equals( " + v2 + " )", c1.equals(c2));
        assertTrue("expected " + v2 + ".equals( " + v1 + " )", c2.equals(c1));
    }

    private void checkVersionsOrder (String v1, String v2) {
        Comparable c1 = newComparable(v1);
        Comparable c2 = newComparable(v2);
        assertTrue("expected " + v1 + " < " + v2, c1.compareTo(c2) < 0);
        assertTrue("expected " + v2 + " > " + v1, c2.compareTo(c1) > 0);
    }

    @Test
    public void mcVersion ()
    {
        checkVersionsOrder(VERSIONS);
    }
}
