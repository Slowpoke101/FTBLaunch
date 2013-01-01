package net.ftb.util;

import javax.swing.UIManager;

import net.ftb.data.LauncherStyle;

public class StyleUtil {
	public static void loadUiStyles() {
		LauncherStyle style = LauncherStyle.getCurrentStyle();
		UIManager.put("control", style.control);
		UIManager.put("text", style.text);
		UIManager.put("nimbusBase", style.nimbusBase);
		UIManager.put("nimbusFocus", style.nimbusFocus);
		UIManager.put("nimbusBorder", style.nimbusBorder);
		UIManager.put("nimbusLightBackground", style.nimbusLightBackground);
		UIManager.put("info", style.info);
		UIManager.put("nimbusSelectionBackground", style.nimbusSelectionBackground);
	}
}
