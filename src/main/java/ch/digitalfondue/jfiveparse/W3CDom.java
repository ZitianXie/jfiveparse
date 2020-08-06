/**
 * Copyright © 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

public class W3CDom {

    public static Document toW3CDocument(ch.digitalfondue.jfiveparse.Document doc) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            //
            setFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature(factory,"http://xml.org/sax/features/external-general-entities", false);
            setFeature(factory,"http://xml.org/sax/features/external-parameter-entities", false);
            setFeature(factory,"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            //
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document d = builder.newDocument();
            doc.traverse(new W3CDNodeVisitor(d));
            return d;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }


    private static void setFeature(DocumentBuilderFactory dbFactory, String feature, boolean value) {
        try {
            dbFactory.setFeature(feature, value);
        } catch (ParserConfigurationException e) {
        }
    }

    public static class W3CDNodeVisitor implements NodesVisitor {

        private static final Set<String> INTERNAL_NAMESPACES = new HashSet<>(
                Arrays.asList(Node.NAMESPACE_HTML,
                        Node.NAMESPACE_SVG,
                        Node.NAMESPACE_MATHML,
                        Node.NAMESPACE_XLINK,
                        Node.NAMESPACE_XMLNS,
                        Node.NAMESPACE_XML
                        ));

        protected final Document document;
        protected org.w3c.dom.Node currentNode;
        protected Deque<Map<String, String>> xmlNamespaces = new ArrayDeque<>();

        public W3CDNodeVisitor(Document document) {
            this.document = document;
            this.currentNode = document;
            this.xmlNamespaces.push(new HashMap<>());
        }

        @Override
        public void start(Node node) {

            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element elem = (Element) node;
                    this.xmlNamespaces.push(new HashMap<>(this.xmlNamespaces.peek()));
                    org.w3c.dom.Element e = toElement(elem);
                    currentNode.appendChild(e);
                    currentNode = e;
                    break;
                case Node.TEXT_NODE:
                    currentNode.appendChild(document.createTextNode(((Text) node).getData()));
                    break;
                case Node.COMMENT_NODE:
                    currentNode.appendChild(document.createComment(((Comment) node).getData()));
                    break;
                /*case Node.DOCUMENT_TYPE_NODE:
                    break;*/
                default:
                    break;
            }
        }

        private static String extractXmlnsPrefix(String xmlns) {
            int idx = xmlns.indexOf(':');
            return idx == -1 ? "" : xmlns.substring(idx + 1);
        }

        private static String extractXmlnsPrefixFromAttrOrElem(String elemOrAttr) {
            int idx = elemOrAttr.indexOf(':');
            return idx == -1 ? "" : elemOrAttr.substring(0, idx);
        }

        private static String extractElementOrAttributeName(String elemOrAttr) {
            int idx = elemOrAttr.indexOf(':');
            return idx == -1 ? elemOrAttr : elemOrAttr.substring(idx + 1);
        }

        protected org.w3c.dom.Element buildNamespacedElement(Element element) {
            String elemPrefix = extractXmlnsPrefixFromAttrOrElem(element.getNodeName());
            String elemName = extractElementOrAttributeName(element.getNodeName());
            String ns = element.getNamespaceURI();
            if (!elemPrefix.isEmpty() && xmlNamespaces.peek().containsKey(elemPrefix)) {
                ns = xmlNamespaces.peek().get(elemPrefix);
            }

            return document.createElementNS(ns, elemName);
        }

        protected org.w3c.dom.Element toElement(Element elem) {
            for (String attrName : elem.getAttributes().keySet()) {
                AttributeNode attr = elem.getAttributeNode(attrName);
                if ("xmlns".equals(attr.getName()) || attr.getName().startsWith("xmlns:")) {
                    xmlNamespaces.peek().put(extractXmlnsPrefix(attr.getName()), attr.getValue());
                }
            }

            org.w3c.dom.Element e = buildNamespacedElement(elem);

            for (String attrName : elem.getAttributes().keySet()) {
                AttributeNode attr = elem.getAttributeNode(attrName);
                if (!("xmlns".equals(attr.getName()) || attr.getName().startsWith("xmlns:"))) {
                    String prefix = extractXmlnsPrefixFromAttrOrElem(attr.getName());
                    String name = extractElementOrAttributeName(attr.getName());
                    String attrNs = prefix.isEmpty() ? attr.getNamespace() : xmlNamespaces.peek().getOrDefault(prefix, attr.getNamespace());
                    Attr copiedAttr = document.createAttributeNS(attrNs, name);
                    copiedAttr.setValue(attr.getValue());
                    copiedAttr.setPrefix(attr.getPrefix());
                    e.setAttributeNodeNS(copiedAttr);
                }
            }
            return e;
        }

        @Override
        public void end(Node node) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                this.xmlNamespaces.pop();
                currentNode = currentNode.getParentNode();
            }
        }

        @Override
        public boolean complete() {
            return false;
        }
    }
}
