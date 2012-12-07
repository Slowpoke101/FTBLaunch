package net.ftb.util;

import javax.swing.JOptionPane;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class ErrorUtils {
	public static void tossError(String output) {
		Logger.logError(output);
		JOptionPane.showMessageDialog(LaunchFrame.getInstance(), output, "ERROR!", JOptionPane.ERROR_MESSAGE);
	}
}
