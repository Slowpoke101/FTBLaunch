package net.ftb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AppUtils {
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
		return getXML(url.openStream());
	}

	/**
	 * Reads XML from a file
	 * @param file the URL to fetch
	 * @return The document
	 * @throws IOException, SAXException if an error occurs when reading from the stream
	 */
	public static Document readXML(File file) throws IOException, SAXException {
		return getXML(new FileInputStream(file));
	}

	/**
	 * Reads XML from a stream
	 * @param stream the stream to read the document from
	 * @return The document
	 * @return The document
	 * @throws IOException, SAXException if an error occurs when reading from the stream
	 */
	public static Document getXML(InputStream stream) throws IOException, SAXException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			return docFactory.newDocumentBuilder().parse(stream);
		} catch (ParserConfigurationException ignored) {
		} catch (UnknownHostException e) { }
		return null;
	}
}
