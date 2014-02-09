package net.ftb.workers;

import java.net.URL;
import java.net.URLClassLoader;

public class CustomCL extends URLClassLoader
{
    public CustomCL(URL[] arg0)
    {
        super(arg0);
    }

    @Override
    public void addURL (URL url)
    {
        super.addURL(url);
    }

}
