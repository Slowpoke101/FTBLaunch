/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.tools;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import net.ftb.log.Logger;

public class PastebinPoster extends Thread {
    @Override
    public void run () {
        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            URL url = new URL("http://pastebin.com/api/api_post.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Cache-Control", "no-transform");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            out = conn.getOutputStream();

            out.write(("api_option=paste" + "&api_dev_key=" + URLEncoder.encode("9a4b85f815457ff6a512c6abad06ea24", "utf-8") + "&api_paste_code=" + URLEncoder.encode(Logger.getLogs(), "utf-8")
                    + "&api_paste_private=" + URLEncoder.encode("0", "utf-8") + "&api_paste_name=" + URLEncoder.encode("", "utf-8") + "&api_paste_expire_date=" + URLEncoder.encode("2D", "utf-8")
                    + "&api_paste_format=" + URLEncoder.encode("text", "utf-8") + "&api_user_key=" + URLEncoder.encode("", "utf-8")).getBytes());
            out.flush();
            out.close();

            if (conn.getResponseCode() == 200) {
                in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    response.append("\r\n");
                }
                reader.close();
                String result = response.toString().trim();
                if (result.matches("^https?://.*")) {
                    Logger.logInfo(result.trim());
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(new URI(result.trim()));
                        } catch (Exception exc) {
                            Logger.logError("Could not open url: " + exc.getMessage());
                        }
                    } else {
                        Logger.logWarn("Could not open url, not supported");
                    }
                } else {
                    String err = result.trim();
                    if (err.length() > 100) {
                        err = err.substring(0, 100);
                    }
                    Logger.logError(err);
                }
            } else {
                Logger.logError("didn't get a 200 response code!");
            }
        } catch (IOException e) {
            Logger.logError(e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
