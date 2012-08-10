package net.ftb.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AppUtils
{
	/**
	 * Returns the stack trace from the given throwable as a string.
	 * @param throwable the throwable to get a stack trace from.
	 * @return a string representing the given throwable's stack trace.
	 */
	public String getStackTrace(Throwable throwable)
	{
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		throwable.printStackTrace(pWriter);
		return sWriter.toString();
	}
}
