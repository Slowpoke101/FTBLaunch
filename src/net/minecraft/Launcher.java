package net.minecraft;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import net.ftb.log.Logger;

public class Launcher extends Applet implements AppletStub {
	private Applet wrappedApplet;
	private URL documentBase;
	private boolean active = false;
	private final Map<String, String> params;

	public Launcher(Applet applet, URL documentBase) {
		params = new TreeMap<String, String>();

		this.setLayout(new BorderLayout());
		this.add(applet, "Center");
		this.wrappedApplet = applet;	
		this.documentBase = documentBase;
	}

	public void setParameter(String name, String value) {
		params.put(name, value);
	}

	public void replace(Applet applet) {
		this.wrappedApplet = applet;

		applet.setStub(this);
		applet.setSize(getWidth(), getHeight());

		this.setLayout(new BorderLayout());
		this.add(applet, "Center");

		applet.init();
		active = true;
		applet.start();
		validate();
	}

	@Override
	public String getParameter(String name) {
		String param = params.get(name);
		if (param != null)
			return param;
		try {
			if(super.getParameterInfo() != null)
				return super.getParameter(name);
		} catch (Exception ignore){
			Logger.logError(ignore.getMessage(), ignore);
		}
		return null;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void appletResize(int width, int height) {
		wrappedApplet.resize(width, height);
	}

	@Override
	public void resize(int width, int height) {
		wrappedApplet.resize(width, height);
	}

	@Override
	public void resize(Dimension d) {
		wrappedApplet.resize(d);
	}

	@Override
	public void init() {
		if (wrappedApplet != null) {
			wrappedApplet.init();
		}
	}

	@Override
	public void start() {
		wrappedApplet.start();
		active = true;
	}

	@Override
	public void stop() {
		wrappedApplet.stop();
		active = false;
	}

	public void destroy() {
		wrappedApplet.destroy();
	}

	@Override
	public URL getCodeBase() {
		return wrappedApplet.getCodeBase();
	}

	@Override
	public URL getDocumentBase() {
		return documentBase;
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		wrappedApplet.setVisible(b);
	}

	public void update(Graphics paramGraphics) { }
	public void paint(Graphics paramGraphics) { }
}

