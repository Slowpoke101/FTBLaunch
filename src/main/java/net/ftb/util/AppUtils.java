/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.util;

import com.google.common.base.Joiner;
import net.ftb.data.Settings;
import net.ftb.log.Logger;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class AppUtils {

    private AppUtils () {
    }

    /**
     * Reads all of the data from the given stream and returns it as a string.
     * @param stream the stream to read from.
     * @return the data read from the given stream as a string.
     */
    public static String readString (InputStream stream) {
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    /**
     * Downloads data from the given URL and returns it as a Document
     * @param url the URL to fetch
     * @return The document
     * @throws IOException, SAXException if an error occurs when reading from the stream
     */
    public static Document downloadXML (URL url) throws IOException, SAXException {
        return getXML(url.openStream());
    }

    /**
     * Reads XML from a stream
     * @param stream the stream to read the document from
     * @return The document
     * @throws IOException, SAXException if an error occurs when reading from the stream
     */
    public static Document getXML (InputStream stream) throws IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            return docFactory.newDocumentBuilder().parse(stream);
        } catch (ParserConfigurationException ignored) {
            Logger.logError(ignored.getMessage(), ignored);
        } catch (UnknownHostException e) {
            Logger.logError(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Simple sleep and check locking scheme
     * @param b boolean to check, continue  when true
     */
    public static void waitForLock (boolean b) {
        waitForLock(b, null);
    }

    /**
     * Simple sleep and check locking scheme
     * @param b boolean to check, continue when true
     * @param s String to use as locking reason for log messages
     */
    public static void waitForLock (boolean b, String s) {
        while (!b) {
            try {
                Thread.sleep(100);
                if (s != null) {
                    Logger.logInfo("Waiting for " + s);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public static void voidInputStream(InputStream is) {
        InputStreamVoider voider = new InputStreamVoider(is);
        voider.start();
    }

    private static class InputStreamVoider extends Thread {
        private InputStream is;

        public InputStreamVoider(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                while (is.read() != -1) {
                    // do nothing just keep stream empty
                }
            } catch (IOException e) {
            }
        }
    }

    public static String MapListToString(Map<String, List<String>> l) {
        Joiner.MapJoiner mapJoiner = Joiner.on('\n').withKeyValueSeparator("=").useForNull("NULL");
        return mapJoiner.join(l);
    }

    public static String ConnectionToString(URLConnection c) {
        boolean failed = false;
        HttpURLConnection conn = (HttpURLConnection) c;
        try {
            if (conn.getErrorStream() != null) {
                return IOUtils.toString(conn.getErrorStream(), Charsets.UTF_8);
            } else if (conn.getInputStream() != null) {
                return IOUtils.toString(conn.getInputStream(), Charsets.UTF_8);
            } else {
                return null;
            }
        } catch (FileNotFoundException e) {
            // ignore this
        } catch (IOException e) {
            failed = true;
        } catch (Exception e) {
            failed = true;
            Logger.logDebug("failed", e);
        }

        if (failed) {
            try {
                return IOUtils.toString(c.getInputStream(), Charsets.UTF_8);
            } catch (Exception e) {
                Logger.logDebug("failed", e);
            }
        }

        return null;
    }

    public static void debugConnection(URLConnection c) {
        debugConnection(c, false);
    }

    public static void debugConnection(URLConnection c, boolean forceDebug) {
        if (Settings.getSettings().getDebugLauncher() || forceDebug) {
            if (!(c instanceof HttpURLConnection)) {
                Logger.logDebug("Something bad just happened.");
            }

            // for IP we need to use reflection or pass url here and rely on dns caching
            // ref: https://community.oracle.com/thread/2149226


            HttpURLConnection conn = (HttpURLConnection) c;
            int responseCode;
            try {
                responseCode = conn.getResponseCode();
            } catch (Exception e) {
                responseCode = -1;
                Logger.logDebug("failed" ,e);
            }


            Logger.logDebug("Request type: " + conn.getRequestMethod());
            Logger.logDebug("URL: " + conn.getURL());
            Logger.logDebug("Response code: " + responseCode);
            Logger.logDebug("Headers:\n" + AppUtils.MapListToString(conn.getHeaderFields()));
            Logger.logDebug("Message body\n" + AppUtils.ConnectionToString(conn));
        }
    }

    public static String getExternalIP() {
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            return in.readLine();
        } catch (Exception e) {
            Logger.logDebug("failed", e);
        }
        return null;
    }

}


