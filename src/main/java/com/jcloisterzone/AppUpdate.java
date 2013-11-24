package com.jcloisterzone;

import java.net.URL;

import org.w3c.dom.Element;

public class AppUpdate {

    private String version;
    private String description;
    private String downloadUrl;

    public static AppUpdate fetch(URL url) {
        Element el = XmlUtils.parseDocument(url).getDocumentElement();
        String version = XmlUtils.childValue(el, "number");
        String description = XmlUtils.childValue(el, "description");
        String downloadUrl = XmlUtils.childValue(el, "url");
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
