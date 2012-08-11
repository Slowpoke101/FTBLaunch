package net.ftb.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class AppUtils
{
	/**
	 * Returns the stack trace from the given throwable as a string.
	 * @param throwable the throwable to get a stack trace from.
	 * @return a string representing the given throwable's stack trace.
	 */
	public static String getStackTrace(Throwable throwable)
	{
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		throwable.printStackTrace(pWriter);
		return sWriter.toString();
	}
	
	/**
	 * Downloads data from the given URL and returns it as a string.
	 * @param url the URL to fetch data from.
	 * @return the data downloaded from the given URL as a string.
	 * @throws IOException if url.openStream throws an IOException
	 */
	public static String downloadString(URL url) throws IOException
	{
		InputStream dlStream = url.openStream();
		StringWriter strWriter = new StringWriter();
		int c;
		while ((c = dlStream.read()) != -1)
		{
			strWriter.write(c);
		}
		
		return strWriter.toString();
	}
}
