package net.ftb.util;

import net.ftb.util.winreg.JavaVersion;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 *
 * Adds the Lets Encrypt cert on older java versions where its not present
 *
 * Based off: https://github.com/Cloudhunter/LetsEncryptCraft
 * With help from: https://stackoverflow.com/questions/34110426/does-java-support-lets-encrypt-certificates
 * And: https://stackoverflow.com/a/31017426
 *
 */
public class LetsEncryptFix {

    public static void fix() {
        //Skip anything older than java 9
        if (!JavaVersion.createJavaVersion(System.getProperty("java.version")).isOlder("1.9")) {
            return;
        }

        String javaVersion = System.getProperty("java.version");
        if(javaVersion.startsWith("1.8")) {
            int javaBuild  = Integer.parseInt(javaVersion.split("_")[1]);
            if(javaBuild >= 101){ // LE was added by default in java 1.8 update 101
                return;
            }
        }

        try {
            addLetsEncryptCertificate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addLetsEncryptCertificate() throws Exception {
        InputStream cert = new BufferedInputStream(LetsEncryptFix.class.getResourceAsStream("/lets-encrypt-x3-cross-signed.der"));


        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final File ks = new File(System.getProperty("java.home") + "/lib/security/cacerts/");

        InputStream inputStream = new FileInputStream(ks);
        keyStore.load(inputStream, "changeit".toCharArray());
        inputStream.close();

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate crt = cf.generateCertificate(cert);

        keyStore.setCertificateEntry("lets-encrypt-x3-cross-signed", crt);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext tls = SSLContext.getInstance("TLS");
        tls.init(null, tmf.getTrustManagers(), null);
        SSLContext.setDefault(tls);
        HttpsURLConnection.setDefaultSSLSocketFactory(tls.getSocketFactory());

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession sslSession) {
                return hostname.equals("dist.creeper.host");
            }
        });
    }

}
