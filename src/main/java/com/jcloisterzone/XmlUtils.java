package com.jcloisterzone;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.SnapshotCorruptedException;


public class XmlUtils {

//    public static Document parseDocument(String fileName) {
//        URL url = XmlUtils.class.getClassLoader().getResource(fileName);
//        return XmlUtils.parseDocument(url);
//    }

    public static Document parseDocument(URL url) {
        try (InputStream is = url.openStream()){
            return XmlUtils.parseDocument(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document parseDocument(InputStream is) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document newDocument() {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static String childValue(Element parent, String childName) {
        NodeList nl = parent.getElementsByTagName(childName);
        if (nl.getLength() == 0) return null;
        return nl.item(0).getTextContent();
    }

    public static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            throw new RuntimeException(te);
        }
        return sw.toString();
    }

    public static String[] asLocation(Element e) {
        return e.getFirstChild().getNodeValue().trim().split("\\s+");
    }

    public static String[] asLocations(Element e, String attr) {
        return e.getAttribute(attr).trim().split("\\s+");
    }

    public static String getTileId(Expansion expansion, Element xml) {
        return expansion.getCode() + "." + xml.getAttribute("id");
    }

    public static  Location union(String[] locations) {
        Location u = null;
        for (String locStr : locations) {
            Location loc = Location.valueOf(locStr);
            u = loc.union(u);
        }
        return u;
    }

    public static boolean attributeBoolValue(Element e, String attr) {
        return e.getAttribute(attr).equals("yes") || e.getAttribute(attr).equals("true") || e.getAttribute(attr).equals("1");
    }

    public static int attributeIntValue(Element e, String attr) {
        return attributeIntValue(e, attr, null);
    }

    public static int attributeIntValue(Element e, String attr, Integer defaultValue) {
        if (!e.hasAttribute(attr)) {
            return defaultValue;
        }
        if (e.getAttribute(attr).equals("yes") || e.getAttribute(attr).equals("true")) {
            return 1;
        }
        return Integer.parseInt(e.getAttribute(attr));
    }

    public static String attributeStringValue(Element e, String attr, String defaultValue) {
        if (!e.hasAttribute(attr)) {
            return defaultValue;
        }
        return e.getAttribute(attr);
    }

    // Snapshot xml utils

    public static Class<?> classForName(String className) throws SnapshotCorruptedException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SnapshotCorruptedException(e);
        }
    }

    public static void injectPosition(Element el, Position p) {
        el.setAttribute("x", "" + p.x);
        el.setAttribute("y", "" + p.y);
    }

    public static Position extractPosition(Element el) {
        int x = Integer.parseInt(el.getAttribute("x"));
        int y = Integer.parseInt(el.getAttribute("y"));
        return new Position(x, y);
    }
}
