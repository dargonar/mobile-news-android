package com.icl.saxon;

import com.icl.saxon.om.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.AnyNameTest;
import com.icl.saxon.expr.Value;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.axis.AttributeEnumeration;
import org.xml.sax.*;
import java.util.*;

/**
  * KeyManager manages the set of key definitions in a stylesheet, and the indexes
  * associated with these key definitions
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public class KeyManager {

    private Hashtable keyList;      // one entry for each named key; the entry contains
                                    // a list of key definitions with that name

    private static Vector emptyVector = new Vector();

    /**
    * create a KeyManager and initialise variables
    */

    public KeyManager() {
        keyList = new Hashtable();
    }

    /**
    * Register a key definition. Note that multiple key definitions with the same name are
    * allowed
    * @param name The absolute name of the key
    * @param keyDefinition The details of the key's definition
    */

    public void setKeyDefinition(KeyDefinition keydef) {
        String name = keydef.getName();
        Vector v = (Vector)keyList.get(name);
        if (v==null) {
            v = new Vector();
            keyList.put(name, v);
        }
        v.addElement(keydef);
    }

    /**
    * Get all the key definitions that match a particular name
    * @param name The absolute name of the required key
    * @return The key definition of the named key if there is one, or null otherwise.
    */

    public Vector getKeyDefinitions(String name) {
        return (Vector)keyList.get(name);
    }

    /**
    * Build the index for a particular document for a named key
    * @param name The absolute name of the required key
    * @param doc The source document in question
    * @param controller The controller
    * @return the index in question, as a Hashtable mapping a key value onto a Vector of nodes
    */

    private synchronized Hashtable buildIndex(String name,
                                           DocumentInfo doc,
                                           Controller controller) throws SAXException {

        Vector definitions = getKeyDefinitions(name);
        if (definitions==null) {
            throw new SAXException("Key " + name + " has not been defined");
        }

        Hashtable index = new Hashtable();

        for (int k=0; k<definitions.size(); k++) {
            constructIndex(doc, index, (KeyDefinition)definitions.elementAt(k), controller);
        }

        return index;
        
    }

    /**
    * Process one key definition to add entries to an index
    */

    private void constructIndex(    DocumentInfo doc,
                                    Hashtable index,
                                    KeyDefinition keydef,
                                    Controller controller) throws SAXException {
                                        
        Pattern match = keydef.getMatch();
        Expression use = keydef.getUse();
        int nodeType = match.getType();
            
        NodeInfo sourceRoot = doc;
        NodeInfo curr = sourceRoot;
        Context c = controller.makeContext(doc);

        if (nodeType==NodeInfo.ATTRIBUTE) {
            while(curr!=null) {
                if (curr instanceof ElementInfo) {
                    AttributeEnumeration atts =
                        new AttributeEnumeration(curr, NodeInfo.ATTRIBUTE, new AnyNameTest());
                    while (atts.hasMoreElements()) {
                        processKeyNode(atts.nextElement(), match, use, index, c);
                    }
                } else {
                    processKeyNode(curr, match, use, index, c);
                }

                curr = curr.getNextInDocument(sourceRoot);
            }

        } else {
            while(curr!=null) {
                processKeyNode(curr, match, use, index, c);
                curr = curr.getNextInDocument(sourceRoot);
            }
        }
    }

    /**
    * Process one node, adding it to the index if appropriate
    */

    private void processKeyNode(NodeInfo curr, Pattern match, Expression use,
                                Hashtable index, Context c) throws SAXException {                                       
        if (match.matches(curr, c)) {
            c.setContextNode(curr);
            c.setPosition(1);
            c.setLast(1);
            Value useval = use.evaluate(c);
            if (useval instanceof NodeSetValue) {
                Vector v = ((NodeSetValue)useval).getVector();
                for (int i=0; i<v.size(); i++) {
                    NodeInfo node = (NodeInfo)v.elementAt(i);
                    String val = node.getValue();
                    Vector nodes = (Vector)index.get(val);
                    if (nodes==null) {
                        nodes = new Vector();
                    }
                    // don't add the same node twice
                    if (nodes.size()==0 || nodes.elementAt(nodes.size()-1)!=curr) {
                        nodes.addElement(curr);
                        index.put(val, nodes);
                    }
                }
            } else {
                String val = useval.asString();
                Vector nodes = (Vector)index.get(val);
                if (nodes==null) nodes = new Vector();
                nodes.addElement(curr);
                index.put(val, nodes);
            }
        }
    }

    /**
    * Get the nodes with a given key value
    * @param name The absolute name of the required key
    * @param doc The source document in question
    * @param value The required key value
    * @param controller The controller, needed only the first time when the key is being built
    * @return a Vector of nodes, always in document order
    */

    public Vector selectByKey(  String name,
                                DocumentInfo doc,
                                String value,
                                Controller controller) throws SAXException {
                                    
        Hashtable index = doc.getKeyIndex(this, name);
        if (index==null) {
            doc.setKeyIndex(this, name, (Object)"under construction");
            index = buildIndex(name, doc, controller);
            doc.setKeyIndex(this, name, (Object)index);
        }
        Vector nodes = (Vector)index.get(value);
        return (nodes==null ? emptyVector : nodes);
    }

}  

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/ 
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License. 
//
// The Original Code is: all this file. 
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved. 
//
// Contributor(s): none. 
//
