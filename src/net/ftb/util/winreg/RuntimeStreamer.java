package net.ftb.util.winreg;
/**
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 *****************************************************************************/

import java.util.*;
import java.io.*;

/**
 * Helper class to fetch the stdout and stderr outputs from started Runtime execs
 * Modified from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 *****************************************************************************/
class RuntimeStreamer extends Thread
{
    InputStream is;
    String lines;

    RuntimeStreamer(InputStream is)
    {
        this.is = is;
        this.lines = "";
    }

    public String contents ()
    {
        return this.lines;
    }

    public void run ()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
            {
                this.lines += line + "\n";
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Execute a command and wait for it to finish
     * @return The resulting stdout and stderr outputs concatenated
     ****************************************************************************/
    public static String execute (String[] cmdArray)
    {
        try
        {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(cmdArray);
            RuntimeStreamer outputStreamer = new RuntimeStreamer(proc.getInputStream());
            RuntimeStreamer errorStreamer = new RuntimeStreamer(proc.getErrorStream());
            outputStreamer.start();
            errorStreamer.start();
            proc.waitFor();
            return outputStreamer.contents() + errorStreamer.contents();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return null;
    }

    public static String execute (String cmd)
    {
        String[] cmdArray = { cmd };
        return RuntimeStreamer.execute(cmdArray);
    }
}
