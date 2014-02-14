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
package net.minecraft;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class Launcher extends Applet implements AppletStub {
    private Applet wrappedApplet;
    private URL documentBase;
    private boolean active = false;
    private final Map<String, String> params;

    public Launcher(Applet applet, URL documentBase) {
        params = new TreeMap<String, String>();
        setLayout(new BorderLayout());
        add(applet, "Center");
        wrappedApplet = applet;
        this.documentBase = documentBase;
    }

    public void setParameter (String name, String value) {
        params.put(name, value);
    }

    public void replace (Applet applet) {
        wrappedApplet = applet;
        applet.setStub(this);
        applet.setSize(getWidth(), getHeight());
        setLayout(new BorderLayout());
        add(applet, "Center");
        applet.init();
        active = true;
        applet.start();
        validate();
    }

    @Override
    public String getParameter (String name) {
        String param = params.get(name);
        if (param != null) {
            return param;
        }
        try {
            if (super.getParameterInfo() != null) {
                return super.getParameter(name);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @Override
    public boolean isActive () {
        return active;
    }

    @Override
    public void appletResize (int width, int height) {
        super.setSize(width, height);
        wrappedApplet.setSize(width, height);
    }

    @Override
    public void init () {
        if (wrappedApplet != null) {
            wrappedApplet.init();
        }
    }

    @Override
    public void start () {
        wrappedApplet.start();
        active = true;
    }

    @Override
    public void stop () {
        wrappedApplet.stop();
        active = false;
    }

    public void destroy () {
        wrappedApplet.destroy();
    }

    @Override
    public URL getCodeBase () {
        return wrappedApplet.getCodeBase();
    }

    @Override
    public URL getDocumentBase () {
        return documentBase;
    }

    @Override
    public void setVisible (boolean b) {
        super.setVisible(b);
        wrappedApplet.setVisible(b);
    }
}
