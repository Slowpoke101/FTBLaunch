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
package net.ftb.gui.panes;

import com.google.common.collect.Lists;

import lombok.Getter;
import net.ftb.data.LauncherStyle;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.ModPackFilterDialog;
import net.ftb.gui.dialogs.PrivatePackDialog;
import net.ftb.locale.I18N;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;

import java.awt.Point;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ThirdPartyPane extends AbstractModPackPane implements ILauncherPane  {
    
    @Getter
	private static ThirdPartyPane instance;

	
	public ThirdPartyPane() {
        super();
        instance = this;
    }

    @Override
    public void onVisible() {
        ThirdPartyPane.getInstance().getPacksScroll().getViewport().setViewPosition(new Point(0, 0));
    }

    boolean filterForTab(ModPack pack) {
        return (pack.isThirdPartyTab() && !pack.getParentXml().contains(Locations.MODPACKXML));
    }

    String getLastPack() {
        return Settings.getSettings().getLastThirdPartyPack();
    }

    String getPaneShortName() {return "Third Party";}

    boolean isFTB() {
        return false;
    }
    
    AbstractModPackPane getThis() {
    	return this;
    }
}
