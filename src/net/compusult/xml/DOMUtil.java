/*
 * DOMUtil.java
 * 
 * Copyright 2013, Compusult Ltd.
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
   
package net.compusult.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtil {

	/**
	 * Like elem.getAttribute(attr), but returns null instead of "" if the attribute is not set.
	 * 
	 * @param elem
	 * @param attr
	 * @return
	 */
	public String getAttributeValue(Element elem, String attr) {
		String value = elem.getAttribute(attr);
		if ("".equals(value) && elem.getAttributeNode(attr) == null) {
			return null;
		}
		return value;
	}
	
	/**
	 * Like elem.getAttributeNS(namespace, attr), but returns null instead of "" if the attribute is not set.
	 * 
	 * @param elem
	 * @param attr
	 * @return
	 */
	public String getAttributeValue(Element elem, String namespace, String attr) {
		String value = elem.getAttributeNS(namespace, attr);
		if ("".equals(value) && elem.getAttributeNodeNS(namespace, attr) == null) {
			return null;
		}
		return value;
	}
	
	/**
	 * @param start
	 * @param ns
	 * @param local
	 * @return
	 */
	public Element findFirstChildNamed(Element start, String ns, String local) {
		
		List<Element> children = findChildrenNamed(start, ns, local);
		if (!children.isEmpty()) {
			return children.get(0);
		}
		
		return null;
	}
	
	/**
	 * @param start
	 * @param ns
	 * @param local
	 * @return
	 */
	public List<Element> findChildrenNamed(Element start, String ns, String local) {
		List<Element> result = new ArrayList<Element>();
		
		NodeList children = start.getChildNodes();
		for (int i = 0, n = children.getLength(); i < n; ++ i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && ns.equals(child.getNamespaceURI()) && local.equals(child.getLocalName())) {
				result.add((Element) child);
			}
		}
		
		return result;
	}
	
	/**
	 * @param start
	 * @return
	 */
	public List<Element> findChildElements(Element start) {
		List<Element> result = new ArrayList<Element>();
		
		NodeList children = start.getChildNodes();
		for (int i = 0, n = children.getLength(); i < n; ++ i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				result.add((Element) child);
			}
		}
		
		return result;
	}
	
	/**
	 * @param node
	 * @return
	 */
	public String nodeTextContent(Node node) {
		if (node == null) {
			return null;
		}
		
		StringBuilder buf = new StringBuilder();
		NodeList children = node.getChildNodes();
		for (int i = 0, n = children.getLength(); i < n; ++ i) {
			if (children.item(i).getNodeType() == Node.TEXT_NODE) {
				if (buf.length() > 0) {
					buf.append(' ');
				}
				buf.append(children.item(i).getNodeValue().trim());
			}
		}
		return buf.toString();
	}

}
