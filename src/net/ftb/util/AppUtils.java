package net.ftb.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AppUtils {
	/**
	 * Returns the stack trace from the given throwable as a string.
	 * @param throwable the throwable to get a stack trace from.
	 * @return a string representing the given throwable's stack trace.
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		throwable.printStackTrace(pWriter);
		return sWriter.toString();
	}

	/**
	 * Reads all of the data from the given stream and returns it as a string.
	 * @param stream the stream to read from.
	 * @return the data read from the given stream as a string.
	 */
	public static String readString(InputStream stream) {
		Scanner scanner = new Scanner(stream).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}

	/**
	 * Writes the given string to the given stream.
	 * @param stream the stream to write to.
	 * @param str the string to write to the stream.
	 */
	public static void writeString(OutputStream stream, String str) {
		new PrintWriter(stream).write(str);
	}

	/**
	 * Downloads data from the given URL and returns it as a string.
	 * @param url the URL to fetch data from.
	 * @return the data downloaded from the given URL as a string.
	 * @throws IOException if an error occurs when reading from the stream.
	 */
	public static String downloadString(URL url) throws IOException {
		return readString(url.openStream());
	}

	/**
	 * Downloads data from the given URL and returns it as a Document
	 * @param url the URL to fetch
	 * @return The document
	 * @throws IOException, SAXException if an error occurs when reading from the stream
	 */
	public static Document downloadXML(URL url) throws IOException, SAXException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			return docFactory.newDocumentBuilder().parse(url.openStream());
		} catch (ParserConfigurationException ignored) { 
		} catch (UnknownHostException e) { }
		return null;
	}
}
