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
package net.feed_the_beast.launcher.json;

import net.feed_the_beast.launcher.json.launcher.RetiredPacks;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RetiredPacksTest {
    RetiredPacks packs;
    File file = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "hiddenpacks.json");

    @Test
    public void factory() {
        try {
            packs = JsonFactory.getRetiredPacks(file);
        } catch (Exception e) {
            fail("Got exception from JsonFactory");
        }
        assertEquals(8, packs.getMapping().size());
        assertTrue(packs.getMapping().containsKey("local_directory"));
    }

}