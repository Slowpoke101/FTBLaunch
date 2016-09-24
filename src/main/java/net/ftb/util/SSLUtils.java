/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
/*
 * Original code from http://nodsw.com/blog/leeland/2006/12/06-no-more-unable-find-valid-certification-path-requested-target
 * Java 7 fix from http://infposs.blogspot.fi/2013/06/installcert-and-java-7.html
 *
 * Adapted code for FTB launcher: removed keychain modification, added more error handling and using launcher's logging system
 */

package net.ftb.util;

import net.ftb.log.Logger;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLUtils {

    private SSLUtils() {
    }

    /**
     *
     * @param s host name to test
     * @param p port to test
     * @throws Exception
     */
    public static void printServerCertChain (String s, int p) {
        boolean handshake_failed = false;
        KeyStore ks;
        SSLSocketFactory factory;
        SSLSocket socket;
        SavingTrustManager tm;

        // fill keychain
        char[] passphrase = "changeit".toCharArray();
        char SEP = File.separatorChar;
        File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
        File file = new File(dir, "cacerts");
        try {
            InputStream in = new FileInputStream(file);
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, passphrase);
            in.close();
        } catch (Exception e) {
            Logger.logWarn("Keychain loadign failed", e);
            return;
        }

        // prepare SSL
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            tm = new SavingTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[] { tm }, null);
            factory = context.getSocketFactory();
        } catch (Exception e) {
            Logger.logWarn("SSL preparation failed", e);
            return;
        }

        //open socket
        try {
            socket = (SSLSocket) factory.createSocket(s, p);
            socket.setSoTimeout(10000);
        } catch (UnknownHostException e) {
            Logger.logWarn("Host lookup failed", e);
            return;
        } catch (Exception e) {
            Logger.logWarn("Generic socket fail" ,e);
            return;
        }

        // and finally initiate SSL handshake
        try {
            socket.startHandshake();
            socket.close();
            Logger.logDebug("SSL handshake was succesfull. Printing certificate chain...");
        } catch (SSLException e) {
            handshake_failed = true;
        } catch (IOException e) {
            Logger.logWarn("IOException", e);
            return;
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            Logger.logDebug("Could not obtain server certificate chain");
            return;
        }

        if (handshake_failed) {
            Logger.logError("SSL handshake failed. Something might be altering SSL certificates");
            Logger.logError("Certificates are not trusted by JVM certificate chain");
            Logger.logError("Certificate chain will be printed in debug logging level");
        }

        Logger.logDebug("");
        Logger.logDebug("Server sent " + chain.length + " certificate(s):");

        Logger.logDebug("");
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            for (int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];
                Logger.logDebug
                        (" " + (i + 1) + " Subject " + cert.getSubjectDN());
                Logger.logDebug("   Issuer  " + cert.getIssuerDN());
                sha1.update(cert.getEncoded());
                Logger.logDebug("   sha1    " + toHexString(sha1.digest()));
                md5.update(cert.getEncoded());
                Logger.logDebug("   md5     " + toHexString(md5.digest()));
                Logger.logDebug("");
            }
        } catch (Exception e) {
            Logger.logDebug("Certificate printing failed" , e);
        }

    }

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(final X509TrustManager tm) {
            this.tm = tm;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
            // throw new UnsupportedOperationException();
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain,
                final String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain,
                final String authType)
                throws CertificateException {
            this.chain = chain;
            this.tm.checkServerTrusted(chain, authType);
        }
    }
}
