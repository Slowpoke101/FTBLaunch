package net.ftb.util;

import javax.swing.JOptionPane;

import net.ftb.gui.LaunchFrame;

public class ErrorUtils {
	public static void tossError(String output) {
		JOptionPane.showMessageDialog(LaunchFrame.getInstance(), output, "ERROR!", JOptionPane.ERROR_MESSAGE);
	}
}
