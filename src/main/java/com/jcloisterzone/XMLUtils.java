package com.jcloisterzone;

import com.jcloisterzone.board.Location;
import io.vavr.Predicates;
import io.vavr.collection.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class XMLUtils {

    private XMLUtils() {}


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


    public static io.vavr.collection.Stream<Node> nodeStream(NodeList nl) {
        List<Node> arrayList = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            arrayList.add(nl.item(i));
        }
        return io.vavr.collection.Stream.ofAll(arrayList);
    }

    public static io.vavr.collection.Stream<Element> elementStream(NodeList nl) {
        return nodeStream(nl).filter(Predicates.instanceOf(Element.class)).map(node -> (Element) node);
    }

    public static io.vavr.collection.Stream<Element> getChildElementStream(Element tileElement) {
        return XMLUtils.elementStream(tileElement.getChildNodes());
    }

    public static io.vavr.collection.Stream<Element> getElementStreamByTagName(Element tileElement, String tagName) {
        return getChildElementStream(tileElement).filter(el -> el.getNodeName().equals(tagName));
    }

    public static Element getElementByTagName(Element parent, String childName) {
        NodeList nl = parent.getElementsByTagName(childName);
        if (nl.getLength() == 0) return null;
        return (Element) nl.item(nl.getLength()-1);
    }


    public static Stream<Location> contentAsLocations(Element e) {
        String[] tokens = e.getFirstChild().getNodeValue().trim().split("\\s+");
        return Stream.of(tokens).map(s -> Location.valueOf(s));
    }

    public static Stream<Location> attrAsLocations(Element e, String attr) {
        String[] tokens = e.getAttribute(attr).trim().split("\\s+");
        return Stream.of(tokens).map(s -> Location.valueOf(s));
    }

    public static Location attrAsLocation(Element e, String attr) {
        String[] tokens = e.getAttribute(attr).trim().split("\\s+");
        if (tokens.length != 1) {
        	throw new IllegalArgumentException("Invalid number of locations. " + e.getAttribute(attr));
        }
        return Location.valueOf(tokens[0]);
    }

    public static boolean attributeBoolValue(Element e, String attr) {
        if (!e.hasAttribute(attr)) return false;
        String val = e.getAttribute(attr);
        if (val.equals("true")) return true;
        if (!val.equals("false")) {
            throw new IllegalArgumentException("only true/false value is allowed for boolean attribute");
        }
        return false;
    }

    public static Integer attributeIntValue(Element e, String attr) {
        return attributeIntValue(e, attr, null);
    }

    public static Integer attributeIntValue(Element e, String attr, Integer defaultValue) {
        if (!e.hasAttribute(attr)) {
            return defaultValue;
        }
        if (e.getAttribute(attr).equals("yes") || e.getAttribute(attr).equals("true")) {
            return 1;
        }
        return Integer.parseInt(e.getAttribute(attr));
    }

    public static String attributeStringValue(Element e, String attr) {
        return attributeStringValue(e, attr, null);
    }

    public static String attributeStringValue(Element e, String attr, String defaultValue) {
        if (!e.hasAttribute(attr)) {
            return defaultValue;
        }
        return e.getAttribute(attr);
    }
}
