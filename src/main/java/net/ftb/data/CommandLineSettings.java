/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.data;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import lombok.Getter;

/**
 * Setting given via command line
 *
 * Note: due a bug in jcommander do not use single long name, e.g. "--help". It will break output of usage()
 *
 * TODO:
 *   * add hints about required values as soon as jcommander supports meta values
 *   * ValidateRequiredValue.validate()'s argument name is always first item in the array. How to fix? will it confuse users?
 *   * "help = true" does not stop Validators from running => ValidateRequiredValue will throw exception even if --help is given
 *
 */
public class CommandLineSettings {
    @Getter
    private static CommandLineSettings settings;

    static {
        settings = new CommandLineSettings();
    }

    @Parameter(names = { "--verbose", "-V" }, description = "Level of verbosity: 0=debug, 1=info, 2=warning, 3=error", arity = 1)
    @Getter
    // default = VERBOSE
    private int verbosity = 0;

    @Parameter(names = { "--log-mc", "-m" }, description = "Show messages from minecraft process")
    @Getter
    private boolean mcLogs = false;

    @Parameter(names = { "--no-console", "-c" }, description = "Do not open console window. (Overrides GUI option.)")
    @Getter
    private boolean noConsole = false;

    @Parameter(names = { "--autostart", "-a" }, description = "Automatically start given pack (WIP feature)", arity = 1)
    @Getter
    private String packDir;

    @Parameter(names = { "--cache-dir", "-C" }, description = "Cache directory", arity = 1, validateWith = ValidateRequiredValue.class)
    @Getter
    private String cacheDir;

    @Parameter(names = { "--dynamic-dir", "-D" }, description = "Dynamic directory", arity = 1, validateWith = ValidateRequiredValue.class)
    @Getter
    private String dynamicDir;

    @Parameter(names = { "--pack-dir", "-P" }, description = "FTB installation directory", arity = 1, validateWith = ValidateRequiredValue.class)
    @Getter
    private String installDir;

    @Parameter(names = { "--use-mac", "-M" }, description = "Use mac address as an encryption key")
    @Getter
    private boolean useMac = false;

    @Parameter(names = { "--help", "-h" }, help = true, description = "Shows help")
    @Getter
    private boolean help = false;

    @Parameter(names = { "--disable-tray", "-t" }, description = "Disable tray icon")
    @Getter
    private boolean disableTray = false;

    public static class ValidateRequiredValue implements IParameterValidator {
        @Override
        public void validate (String name, String value) throws ParameterException {
            if (value == null || value.isEmpty()) {
                // this should never happen because jcommander bug
                throw new ParameterException("Expected a value after parameter " + name);
            }
            if (value.startsWith("-") && !value.equals("--")) {
                throw new ParameterException("Expected a value after parameter " + name + ". Looks like argument " + value);
            }
        }
    }
}