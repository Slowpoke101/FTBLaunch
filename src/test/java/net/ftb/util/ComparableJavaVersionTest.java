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
public class ComparableJavaVersionTest {
    private Comparable newComparable (String version) {
        return new ComparableVersion(version);
    }

    private static final String[] VERSIONS =
            { "1.6.0_31", "1.6.0_60", "1.7.0_65", "1.8.0", "1.8.0_25", "1.8.0_40-ea", "1.8.0_40", "1.9.0-ea" };

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
    public void javaVersion ()
    {
        checkVersionsOrder( VERSIONS );
    }
}
