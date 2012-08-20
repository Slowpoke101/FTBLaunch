package net.ftb.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
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
	 * Reads all of the data from the given stream and returns it as a string.
	 * @param stream the stream to read from.
	 * @return the data read from the given stream as a string.
	 * @throws IOException if an error occurs when reading from the stream.
	 */
	public static String readString(InputStream stream) 
			throws IOException
	{
		StringWriter writer = new StringWriter();
		int c;
		while ((c = stream.read()) != -1)
		{
			writer.write(c);
		}
		
		return writer.toString();
	}
	
	/**
	 * Writes the given string to the given stream.
	 * @param stream the stream to write to.
	 * @param str the string to write to the stream.
	 * @throws IOException if an error occurs when writing to the stream.
	 */
	public static void writeString(OutputStream stream, String str) 
			throws IOException
	{
		StringReader reader = new StringReader(str);
		int c;
		while ((c = reader.read()) != -1)
		{
			stream.write(c);
		}
	}
	
	/**
	 * Downloads data from the given URL and returns it as a string.
	 * @param url the URL to fetch data from.
	 * @return the data downloaded from the given URL as a string.
	 * @throws IOException if an error occurs when reading from the stream.
	 */
	public static String downloadString(URL url) throws IOException
	{
		return readString(url.openStream());
	}
}
