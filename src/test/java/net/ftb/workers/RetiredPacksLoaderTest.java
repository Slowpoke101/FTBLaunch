/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.workers;

import com.google.common.base.Throwables;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;

@PrepareForTest({ ModPack.class, Settings.class })
public class RetiredPacksLoaderTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    File json = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "hiddenpacks.json");
    File sourceFolder = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "files_for_retired_packs_loader");
    File temporaryFolder;
    RetiredPacksLoader loader;

    @Test
    public void fullMock() throws Exception{
        temporaryFolder = tf.newFolder();
        FileUtils.copyDirectory(sourceFolder, temporaryFolder);
        loader = new RetiredPacksLoader(json.toURL(), temporaryFolder.getAbsolutePath(), temporaryFolder.getAbsolutePath());

        //*******************************************
        // fully mock Settings and ModPack classes with failing default answers
        //*******************************************
        Settings mockedSettings = Mockito.mock(Settings.class, new RuntimeExceptionAnswer());
        PowerMockito.mockStatic(Settings.class, new RuntimeExceptionAnswer());
        //not needed (yet) ModPack mockedModPack = Mockito.mock(ModPack.class, new RuntimeExceptionAnswer());
        PowerMockito.mockStatic(ModPack.class, new RuntimeExceptionAnswer());

        // mock singleton getter to return mocked instance of the Settings class
        // doReturn/doNothing/... is required instead of when() because we are re-stubbing methods
        PowerMockito.doReturn(mockedSettings).when(Settings.class, "getSettings");

        // *******************************************
        // mock functions which are called from CUT...
        //
        // TODO: * Write external classes for default mocking of Settings/Modpack/CommandLineArguments default mocking with sane default values
        //       * Move common stubs in @Setup
        //       * Add tests: e.g. return some pack code for Settings.getSettings().getPrivatePacks()
        // *******************************************
        //Mockito.doReturn(new ArrayList<String>()).when(mockedSettings).getPrivatePacks();
        PowerMockito.doNothing().when(ModPack.class, "loadXml", anyString());
        Mockito.doNothing().when(mockedSettings).addPrivatePack(anyString());
        Mockito.doNothing().when(mockedSettings).save();
        Mockito.doReturn(new ArrayList<String>()).when(mockedSettings).getPrivatePacks();
        //Mockito.doCallRealMethod().when(mockedSettings).finalize();

        // run the "thread"
        loader.run();

        // check if stubbed methods were called
        // TODO: check that functions are not called with other arguments
        PowerMockito.verifyStatic();
        ModPack.loadXml("RPGImmersion.xml");
        Mockito.verify(mockedSettings).addPrivatePack("RPGImmersion");
        Mockito.verify(mockedSettings).save();
    }

    @Test
    public void realSetting() throws Exception{
        temporaryFolder = tf.newFolder();
        FileUtils.copyDirectory(sourceFolder, temporaryFolder);
        loader = new RetiredPacksLoader(json.toURL(), temporaryFolder.getAbsolutePath(), temporaryFolder.getAbsolutePath());

        //*******************************************
        // fully mock Settings and ModPack classes with failing default answers
        //*******************************************
        //Settings mockedSettings = Mockito.mock(Settings.class, new RuntimeExceptionAnswer());
        Settings mockedSettings_real = new Settings(new File(temporaryFolder, "not_exists"));
        Settings mockedSettings = Mockito.spy(mockedSettings_real);
        PowerMockito.mockStatic(Settings.class, new RuntimeExceptionAnswer());
        //not needed (yet) ModPack mockedModPack = Mockito.mock(ModPack.class, new RuntimeExceptionAnswer());
        PowerMockito.mockStatic(ModPack.class, new RuntimeExceptionAnswer());

        // mock singleton getter to return mocked instance of the Settings class
        // doReturn/doNothing/... is required instead of when() because we are re-stubbing methods
        PowerMockito.doReturn(mockedSettings).when(Settings.class, "getSettings");

        // *******************************************
        // mock functions which are called from CUT...
        //
        // TODO: * Write external classes for default mocking of Settings/Modpack/CommandLineArguments default mocking with sane default values
        //       * Move common stubs in @Setup
        //       * Add tests: e.g. return some pack code for Settings.getSettings().getPrivatePacks()
        // *******************************************
        //Mockito.doReturn(new ArrayList<String>()).when(mockedSettings).getPrivatePacks();
        PowerMockito.doNothing().when(ModPack.class, "loadXml", anyString());
        //Mockito.doNothing().when(mockedSettings).addPrivatePack(anyString());
        Mockito.doNothing().when(mockedSettings).save();

        // run the "thread"
        loader.run();

        // check if stubbed methods were called
        // TODO: check that functions are not called with other arguments
        PowerMockito.verifyStatic();
        ModPack.loadXml("RPGImmersion.xml");
        Mockito.verify(mockedSettings).addPrivatePack("RPGImmersion");
        Mockito.verify(mockedSettings).save();
    }

    public static class RuntimeExceptionAnswer implements Answer {
        @Override
        public Object answer (InvocationOnMock invocation) throws Throwable {
            System.out.println(invocation.getMethod().toString() + ": " + Arrays.toString(invocation.getArguments()));
            Exception e = new RuntimeException();
            System.out.println(ExceptionUtils.getStackTrace(e));
            fail();
            return null;
        }
    }
}
