package net.ftb.download.info;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.ftb.download.Locations;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class AssetInfo extends DownloadInfo {
    public final String etag;

    private AssetInfo(File root, Element node) throws MalformedURLException {
        url = new URL(Locations.mc_res + getText(node, "Key", null));
        name = getText(node, "Key", "");
        etag = getText(node, "ETag", "").replace("\"", "");
        size = Long.parseLong(getText(node, "Size", "0"));
        local = new File(root, name);
    }

    private String getText (Element node, String name, String def) {
        NodeList lst = node.getElementsByTagName(name);
        if (lst == null)
            return def;
        return lst.item(0).getChildNodes().item(0).getNodeValue();
    }

    public String toString () {
        return etag + " " + name + " " + size;
    }
}
