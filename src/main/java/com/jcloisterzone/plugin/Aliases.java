package com.jcloisterzone.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcloisterzone.XMLUtils;

public class Aliases {

    private final Map<String, String> geometryAliases = new HashMap<>();
    private final Map<String, String> imageAliases = new HashMap<>();

    public Aliases(ClassLoader loader, String folder) throws IOException, SAXException, ParserConfigurationException {
        NodeList nl;
        URL aliasesResource = loader.getResource(folder + "/aliases.xml");
        if (aliasesResource != null) {
            Element aliasesEl = XMLUtils.parseDocument(aliasesResource).getDocumentElement();
            nl = aliasesEl.getElementsByTagName("alias");
            for (int i = 0; i < nl.getLength(); i++) {
                Element alias = (Element) nl.item(i);
                String forTile = alias.getAttribute("for");
                String useAll = alias.getAttribute("use");
                String useGeometry = alias.getAttribute("useGeometry");
                String useImage = alias.getAttribute("useImage");
                if (useGeometry.isEmpty()) {
                    useGeometry = useAll;
                }
                if (useImage.isEmpty()) {
                    useImage = useAll;
                }
                if (!useGeometry.isEmpty()) {
                    geometryAliases.put(forTile, useGeometry);
                }
                if (!useImage.isEmpty()) {
                    imageAliases.put(forTile, useImage);
                }
            }
        }
    }

    public String getImageAlias(String tileId) {
        return imageAliases.get(tileId);
    }

    public String getGeometryAlias(String tileId) {
        return geometryAliases.get(tileId);
    }
}
