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
package net.ftb.data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

import javax.imageio.ImageIO;
import javax.swing.*;

public class LauncherStyle extends Properties {

    //TODO bump this???
    private static final long serialVersionUID = 6370446700503387209L;

    private static LauncherStyle currentStyle;

    private static File baseStylePath;

    static {
        baseStylePath = new File(OSUtils.getDynamicStorageLocation(), "launcher_styles");

        if (!baseStylePath.exists()) {
            baseStylePath.mkdir();
        }

        currentStyle = new LauncherStyle();
        currentStyle.load();
    }

    public Color control = new Color(40, 40, 40);
    public Color text = new Color(40, 40, 40).brighter().brighter().brighter().brighter().brighter();
    public Color nimbusBase = new Color(0, 0, 0);
    public Color nimbusFocus = new Color(40, 40, 40);
    public Color nimbusBorder = new Color(40, 40, 40);
    public Color nimbusLightBackground = new Color(40, 40, 40);
    public Color info = new Color(40, 40, 40).brighter().brighter();
    public Color nimbusSelectionBackground = new Color(40, 40, 40).brighter().brighter();
    public Color footerColor = new Color(25, 25, 25);
    public Color filterTextColor = new Color(243, 119, 31);
    public Color filterInnerTextColor = new Color(255, 255, 255);
    public Color tabPaneBackground = new Color(255, 255, 255, 0);
    public Color tabPaneForeground = new Color(255, 255, 255);
    public Color headerImageColor = new Color(255, 255, 255);
    public Color headerColor = new Color(243, 119, 31);
    public Color headerImageHighlightColor = new Color(236, 26,61);
    public Color headerHighlightColor = new Color(236, 26,61);


    public void load () {
        this.load(Settings.getSettings().getStyle());
    }

    public void load (String file) {
        File filename = new File(baseStylePath, file);
        if (new File(baseStylePath, file).exists()) {
            try {
                this.load(new FileReader(filename));
            } catch (FileNotFoundException e) {
                Logger.logWarn("Could not load style", e);
            } catch (IOException e) {
                Logger.logWarn("Could not load style", e);
            }
        }

        this.control = loadColor("control", this.control);
        this.text = loadColor("text", this.text);
        this.nimbusBase = loadColor("nimbusBase", this.nimbusBase);
        this.nimbusFocus = loadColor("nimbusFocus", this.nimbusFocus);
        this.nimbusBorder = loadColor("nimbusBorder", this.nimbusBorder);
        this.nimbusLightBackground = loadColor("nimbusLightBackground", this.nimbusLightBackground);
        this.info = loadColor("info", this.info);
        this.nimbusSelectionBackground = loadColor("nimbusSelectionBackground", this.nimbusSelectionBackground);
        this.footerColor = loadColor("footerColor", this.footerColor);
        this.filterTextColor = loadColor("filterTextColor", this.filterTextColor);
        this.filterInnerTextColor = loadColor("filterInnerTextColor", this.filterInnerTextColor);
        //todo add all the new color swap stuffs in here!!!
        try {
            this.store(new FileOutputStream(filename), "FTB Style File");
        } catch (FileNotFoundException e) {
            Logger.logWarn("Could not save style", e);
        } catch (IOException e) {
            Logger.logWarn("Could not save style", e);
        }
    }

    public static String getColorAsString (Color col) {
        return col.getRed() + "," + col.getGreen() + "," + col.getBlue();
    }

    public static Color getStringAsColor (String str) {
        String[] tokens = str.split(",");
        return new Color(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
    }

    private Color loadColor (String string, Color defaultColor) {
        String defaultColorStr = getColorAsString(defaultColor);
        if (!this.containsKey(string)) {
            this.setProperty(string, defaultColorStr);
        }
        return getStringAsColor(this.getProperty(string, defaultColorStr));
    }

    public static LauncherStyle getCurrentStyle () {
        return currentStyle;
    }

    public static void setCurrentStyle (LauncherStyle currentStyle) {
        LauncherStyle.currentStyle = currentStyle;
    }

    //convienience method for handling header icons
    public ImageIcon filterHeaderIcon(URL u){
        try {
            return changeColor(ImageIO.read(u), headerImageColor, headerColor, headerImageHighlightColor, headerHighlightColor);
        }catch(Exception e){
            Logger.logWarn("error changing colors, using default instead");
        }
        return new ImageIcon(u);
    }
    public ImageIcon changeColor(BufferedImage image, Color toReplace, Color newColor, Color toReplace2, Color newColor2) {
        BufferedImage destImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = destImage.createGraphics();
        g.drawImage(image, null, 0, 0);
        g.dispose();
        for (int i = 0; i < destImage.getWidth(); i++) {
            for (int j = 0; j < destImage.getHeight(); j++) {
                Color c = new Color(image.getRGB(i,j), true);
                if (toReplace != null && compareColors(toReplace, c)) {
                    destImage.setRGB(i, j, getRGB(newColor, c));
                }
                if (toReplace2!= null && compareColors(toReplace2, c)) {
                    destImage.setRGB(i, j,  getRGB(newColor2, c));
                }

            }
        }
        return new ImageIcon(destImage);
    }
    public boolean compareColors (Color b, Color c){
        //ignores transparency, we add this back in in the getRGB method
        return c.getBlue() == b.getBlue() && c.getRed() == b.getRed() && c.getGreen() == b.getGreen();
    }
    public int getRGB(Color nw, Color oldColorWithTrans){
        return new Color(nw.getRed(), nw.getGreen(), nw.getBlue(), oldColorWithTrans.getAlpha()).getRGB();
    }
}
