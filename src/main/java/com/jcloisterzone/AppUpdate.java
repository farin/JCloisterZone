package com.jcloisterzone;

import java.net.URL;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AppUpdate {

    private String version;
    private String description;
    private String downloadUrl;

    public static AppUpdate fetch(URL url) {
        Element el = XmlUtils.parseDocument(url).getDocumentElement();
        String version = XmlUtils.childValue(el, "number");
        String description = XmlUtils.childValue(el, "description");

        boolean isWin = System.getProperty("os.name").startsWith("Win");
        String downloadUrl = null;
        NodeList nl = el.getElementsByTagName("url");
        for (int i = 0; i < nl.getLength(); i++) {
            String value = nl.item(i).getTextContent().trim();
            boolean isZip = value.endsWith(".zip");
            if (downloadUrl == null || (isWin == isZip)) {
                downloadUrl = value;
            }
        }
        return new AppUpdate(version, description, downloadUrl);
    }

    public AppUpdate(String version, String description, String downloadUrl) {
        this.version = version;
        this.description = description;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return this.version + " " + this.description;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDownloadUrl() {
        return downloadUrl;
    }
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }


}
