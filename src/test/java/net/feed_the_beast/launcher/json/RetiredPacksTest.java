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