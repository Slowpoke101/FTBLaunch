package net.ftb.util.winreg;

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

import net.ftb.util.ComparableVersion;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

@PrepareForTest( RuntimeStreamer.class )
public class JavaInfoTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void badOutPutFromCommandLine() {
        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn("");
        JavaInfo j = JavaInfo.getJavaInfo("x");
        assertEquals(null, j);
    }

    @Test
    public void javaExecutableNotFound() {
        JavaInfo j = JavaInfo.getJavaInfo("not real path. just trying something");
        assertEquals(null, j);
    }

    String java8_32bit =
            "java version \"1.8.0_25\"\n"
                    + "Java(TM) SE Runtime Environment (build 1.8.0_25-b18)\n"
                    + "Java HotSpot(TM) Client VM (build 25.25-b02, mixed mode)\n";

    String java7_64bit_openjdk = "java version \"1.7.0_65\"\n"
            + "OpenJDK Runtime Environment (IcedTea 2.5.3) (7u71-2.5.3-1)\n"
            + "OpenJDK 64-Bit Server VM (build 24.65-b04, mixed mode)\n";

    @Test
    public void java8_32bit_parsing() {
        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn(java8_32bit);

        JavaInfo j = null;
        try {
            j = JavaInfo.getJavaInfo("java8_32bit_parsing");
        } catch (Exception e) {
            fail("Unexcepted Exception");
        }

        assertEquals(false, j.is64bits);
        assertEquals(1, j.getMajor());
        assertEquals(8, j.getMinor());
        assertEquals(0, j.getRevision());
        assertEquals(25, j.getUpdate());

        assertEquals("java8_32bit_parsing", j.path);
        assertEquals(new ComparableVersion("1.8.0_25"), j.comparableVersion );
    }

    @Test
    public void java7_65bit_openjdk_parsing() {
        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn(java7_64bit_openjdk);

        JavaInfo j = null;
        try {
            j = JavaInfo.getJavaInfo("java7_65bit_openjdk_parsing");
        } catch (Exception e) {
            fail("Unexcepted Exception");
        }

        assertEquals(true, j.is64bits);
        assertEquals(1, j.getMajor());
        assertEquals(7, j.getMinor());
        assertEquals(0, j.getRevision());
        assertEquals(65, j.getUpdate());

        assertEquals("java7_65bit_openjdk_parsing", j.path);
        assertEquals(new ComparableVersion("1.7.0_65"), j.comparableVersion );
    }

    /*
     * TODO: add more proper tests for sorting.
     */
    @Test
    public void sorting() {
        JavaInfo j7, j8;

        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn(java7_64bit_openjdk);
        j7 = JavaInfo.getJavaInfo("sorting_j7");

        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn(java8_32bit);
        j8 = JavaInfo.getJavaInfo("sorting_j8");

        // ATT: bitness
        assertEquals(true,  JavaInfo.PREFERRED_SORTING.compare(j7, j8) > 0);

        // ATT: simpler comparison method
        assertEquals(true, JavaVersion.PREFERRED_SORTING.compare(j7, j8) < 0);

        JavaVersion j7_99;
        JavaVersion j7_65;
        j7_99 = JavaVersion.createJavaVersion("1.7.0_99");
        j7_65 = JavaVersion.createJavaVersion("1.7.0_65");

        // test comparison methods, also test polymorphism
        assertEquals(true, JavaVersion.PREFERRED_SORTING.compare(j7, j7_99) < 0 );
        //info, version
        assertEquals(true, j7.isOlder(j7_99));
        assertEquals(false, !j7.isOlder(j7_99));
        //version, info
        assertEquals(true, !j7_99.isOlder(j7));
        //info, version
        assertEquals(false, j7.isSameVersion(j7_99));
        //info, version
        assertEquals(true, j7.isSameVersion(j7_65));
        //version, version
        assertEquals(true, j7_65.isSameVersion(j7_65));
        //info, info
        assertEquals(true, j7.isSameVersion(j7));

        // dummy test
        assertEquals(true, j7.isOlder(JavaVersion.createJavaVersion("1.7.0_9999999")));
        assertEquals(true, j7.isOlder("1.7.0_9999999"));
        assertEquals(true, j7.isSameVersion("1.7.0_65"));
    }

}