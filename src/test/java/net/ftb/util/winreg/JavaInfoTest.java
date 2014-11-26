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
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

@PrepareForTest( RuntimeStreamer.class )
public class JavaInfoTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test(expected = Exception.class)
    public void badOutPutFromCommandLine() throws Exception{
        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn("");
        JavaInfo j = new JavaInfo("x");
    }

    @Test
    public void badOutPutFromCommandLine2() {
        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn("");
        try {
            JavaInfo j = new JavaInfo("x");
            fail("Excepted exception");
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void javaExecutableNotFound() {
        try {
            JavaInfo j = new JavaInfo("not real path. just trying something");
            fail("Excepted exception");
        } catch (Exception e) {
            //
        }
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
            j = new JavaInfo("XX");
        } catch (Exception e) {
            fail("Unexcepted Exception");
        }

        assertEquals(false, j.is64bits);
        assertEquals(1, j.getMajor());
        assertEquals(8, j.getMinor());
        assertEquals(0, j.getRevision());
        assertEquals(25, j.getUpdate());

        assertEquals("XX", j.path);
        assertEquals(new ComparableVersion("1.8.0_25"), j.comparableVersion );
    }

    @Test
    public void java7_65bit_openjdk_parsing() {
        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn(java7_64bit_openjdk);

        JavaInfo j = null;
        try {
            j = new JavaInfo("XX");
        } catch (Exception e) {
            fail("Unexcepted Exception");
        }

        assertEquals(true, j.is64bits);
        assertEquals(1, j.getMajor());
        assertEquals(7, j.getMinor());
        assertEquals(0, j.getRevision());
        assertEquals(65, j.getUpdate());

        assertEquals("XX", j.path);
        assertEquals(new ComparableVersion("1.7.0_65"), j.comparableVersion );
    }

    /*
     * TODO: add more proper tests for sorting.
     */
    @Test
    public void samePath() {
        JavaInfo j7 = null, j8 = null;

        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn(java7_64bit_openjdk);
        try {
            j7 = new JavaInfo("XX");
        } catch (Exception e) {
            fail("Unexcepted Exception");
        }

        stub(method(RuntimeStreamer.class, "execute", String[].class)).toReturn(java8_32bit);
        try {
            j8 = new JavaInfo("XX");
        } catch (Exception e) {
            fail("Unexcepted Exception");
        }

        // ATT: bitness
        assertEquals(true,  JavaInfo.PREFERRED_SORTING.compare(j7, j8) > 0);

        // ATT: simpler comparison method
        assertEquals(true, JavaVersion.PREFERRED_SORTING.compare(j7, j8) < 0);

        JavaVersion j7_99 = null;
        try {
            j7_99 = new JavaVersion("1.7.0_99", false);
        } catch (Exception e) {
            fail("Unexcepted exception");
        }
        assertEquals(true, JavaVersion.PREFERRED_SORTING.compare(j7, j7_99) < 0 );
    }
}