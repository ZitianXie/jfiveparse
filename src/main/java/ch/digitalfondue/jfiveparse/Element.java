/**
 * Copyright (C) 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent an Element (e.g. "&lt;div&gt;").
 */
public class Element extends Node {

    private String nodeName;
    String originalNodeName;
    private String namespaceURI;
    private Attributes attributes;

    private List<Node> childNodes = null;

    /**
     * Create an element in the {@link Node#NAMESPACE_HTML} namespace with no
     * attributes.
     * 
     * @param name
     */
    public Element(String name) {
        this(name, Node.NAMESPACE_HTML);
    }

    /**
     * Create an element in the given namespace with no attributes.
     * 
     * @param name
     * @param nameSpace
     */
    public Element(String name, String nameSpace) {
        this(name, nameSpace, null);
    }

    /**
     * Create an element in the given namespace with attributes.
     * 
     * @param name
     * @param nameSpace
     * @param attributes
     */
    public Element(String name, String nameSpace, Attributes attributes) {
        this(name, name, nameSpace, attributes);
    }
    
    Element(String name, String originalName, String nameSpace, Attributes attributes) {
        this.nodeName = name;
        this.originalNodeName = originalName;
        this.namespaceURI = nameSpace;
        this.attributes = attributes;
    }

    private void ensureAttributesPresence() {
        if (attributes == null) {
            this.attributes = new Attributes();
        }
    }

    /**
     * @see Node#getChildNodes().
     */
    @Override
    public List<Node> getChildNodes() {
        return childNodes == null ? Collections.<Node> emptyList() : Collections.unmodifiableList(childNodes);
    }

    @Override
    List<Node> getMutableChildNodes() {
        if (childNodes == null) {
            this.childNodes = new ArrayList<>(2);
        }
        return childNodes;
    }

    @Override
    public byte getNodeType() {
        return ELEMENT_NODE;
    }

    /**
     * Return the node name. If the {@link Element} is a "div" element, the
     * {@link String} "div" will be returned.
     */
    @Override
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Return the namespace.
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    boolean is(String name, String nameSpace) {
        return this.nodeName.equals(name) && this.namespaceURI.equals(nameSpace);
    }

    /**
     * Get the {@link Attributes}.
     */
    public Attributes getAttributes() {
        ensureAttributesPresence();
        return attributes;
    }

    /**
     * See {@link #getNodeName()}.
     */
    @Override
    public String toString() {
        return nodeName;
    }
}
