package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.output.*;
import java.util.*;
import org.xml.sax.*;
import java.io.Writer;


/**
* This class represents a Value of type result tree fragment. <BR>
* A Result Tree Fragment can be created by defining a variable in XSL whose value is defined by
* the contents of the xsl:variable element, possibly including start and end element tags. <BR>
* @version 1 Sep 1999: complete rewrite; a result tree fragment is now implemented as a structure
* of nodes rather than as a String.
* @version 14 Dec 1999: Removed the unnecessary document element.
* @version 25 February 2000: uses the Builder class rather than building the tree itself.
* @version 29 February 2000: complete change of approach. Building the tree turned out to be
* very expensive. So it now just keeps a record of the Emitter events, and only builds the tree
* when the node-set() extension function is used. This representation exploits the fact that there
* are only two operations defined on a result tree fragment in the XSLT standard: conversion to
* a string, and copying to another tree.
* @version 11 September 2000: A result tree fragment is now a kind of NodeSetValue
*/

public class FragmentValue extends SingletonNodeSet implements Emitter {

    //private DocumentInfo docNode;
    //private Builder builder;
    private char[] buffer = new char[4096];
    private int used = 0;
    private Vector events = new Vector(20, 20);
    private String baseURI = null;

    private static AttributeCollection emptyAttributeCollection = new AttributeCollection();

    private static Integer START_ELEMENT = new Integer(1);
    private static Integer END_ELEMENT = new Integer(2);
    private static Integer START_NAMESPACE = new Integer(3);
    private static Integer END_NAMESPACE = new Integer(4);
    private static Integer CHARACTERS = new Integer(5);
    private static Integer PROCESSING_INSTRUCTION = new Integer(6);
    private static Integer COMMENT = new Integer(7);
    private static Integer ESCAPING_ON = new Integer(8);
    private static Integer ESCAPING_OFF = new Integer(9);

    public FragmentValue() throws SAXException {
    }

    /**
    * Constructor: create a result tree fragment containing a single text node
    * @param value: a String containing the value
    */

    public FragmentValue(String value) throws SAXException {
        buffer = value.toCharArray();
        used = value.length();
    }

    /**
    * Set the Base URI for the nodes in the result tree fragment. This is defined to be
    * the Base URI of the relevant xsl:variable element in the stylesheet.
    */

    public void setBaseURI(String uri) {
        baseURI = uri;
    }

    /**
    * Get an Emitter that can be used to feed data to this result tree fragment
    */

    public Emitter getEmitter() throws SAXException {
        return this;
    }

    /**
    * Convert the result tree fragment to a string.
    */

    public String asString() throws SAXException {
        return new String(buffer, 0, used);
    }

    /**
    * Convert the result tree fragment to a number
    */

    public double asNumber() throws SAXException {
        return Value.stringToNumber(asString());
    }

    /**
    * Convert the result tree fragment to a boolean
    */

    public boolean asBoolean() throws SAXException {
        return true;
    }

    /**
    * Count the nodes in the node-set. 
    */

    public int getCount() throws SAXException {
        return 1;
    }

    /**
    * Determine whether the node-set is empty. This is more efficient than testing getCount()==0,
    * because it doesn't risk triggering a sort.
    */

    public boolean isEmpty() throws SAXException {
        return false;
    }

    /**
    * Determine whether the node-set is singular, that is, whether it has a single member.
    * This is more efficient that testing getCount()==1, because it doesn't risk triggering a sort.
    */

    public boolean isSingular() throws SAXException {
        return true;
    }
    
    /**
    * Determine whether a particular node is present in the nodeset.
    */

    public boolean contains (NodeInfo node) throws SAXException {
        if (this.node==null) return false;  // can't be true if we haven't formed the node-set yet!
        return node==this.node;
    }

    /**
    * Simplify the expression
    */

    public Expression simplify() throws SAXException {
        // overrides method on superclass
        return this;
    }

    /**
    * Return the nodes in the node-set as a Vector. 
    * @return a Vector containing the NodeInfo object representing the root nodes 
    */

    public Vector getVector() throws SAXException {
        Vector v = new Vector(1);
        v.addElement(getRootNode());
        return v;
    }

    /**
    * Return the nodes in the node-set as an array. 
    * @return an array containing the NodeInfo object representing the root 
    */

    public NodeInfo[] getNodes() throws SAXException {
        NodeInfo[] nodes = new NodeInfo[1];
        nodes[0] = getRootNode();
        return nodes;
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node
    */

    public NodeInfo getFirst() throws SAXException {
        return getRootNode();
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() throws SAXException {
        return new SingletonEnumeration(getRootNode());
    }

    /**
    * Test whether a nodeset "equals" another Value
    */

    public boolean equals(Value other) throws SAXException {

        if (other instanceof NodeSetValue) {

            // see if there is a node in A with the same string value as a node in B
            
            String value = asString();
            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            while (e2.hasMoreElements()) {
                if (e2.nextElement().getValue().equals(value)) return true;
            }
            return false;

        } else if (other instanceof NumericValue) {
                 return asNumber()==other.asNumber();
                
        } else if (other instanceof StringValue || other instanceof ObjectValue) {
                 return asString().equals(other.asString());

        } else if (other instanceof BooleanValue) {                                
                 return other.asBoolean();
                
        } else {
                throw new SAXException("Unknown data type in a relational expression");
        }
    }

    /**
    * Test whether a nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws SAXException {
                
        if (other instanceof NodeSetValue) {

            String value = asString();
            
            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            while (e2.hasMoreElements()) {
                if (!e2.nextElement().getValue().equals(value)) return true;
            }
            return false;

        } else if (other instanceof NumericValue) {
             return asNumber()!=other.asNumber();
                
        } else if (other instanceof StringValue || other instanceof ObjectValue) {
             return !asString().equals(other.asString());

        } else if (other instanceof BooleanValue) {                                
             return !other.asBoolean();
                
        } else {
                throw new SAXException("Unknown data type in a relational expression");

        }
    }

    /**
    * Test how a FragmentValue compares to another Value under a relational comparison.
    */

    public boolean compare(int operator, Value other) throws SAXException {
        if (operator==Tokenizer.EQUALS) return equals(other);
        if (operator==Tokenizer.NE) return notEquals(other);

        return (new NumericValue(asNumber())).compare(operator, other);
    }

    /**
    * Return the type of the value 
    * @return  Value.NODESET (always)
    */

    public int getType() {
        return Value.NODESET;
    }
    
    /**
    * Determine the data type of the expression, if possible
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
    }
    
    /**
    * Get a string representation of the expression
    */

    public String toString() {
        return "** RESULT TREE FRAGMENT **";
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Implement the Emitter interface
    //////////////////////////////////////////////////////////////////////////////////
    
    boolean previousCharacters;

    /**
    * Notify document start
    */

    public void startDocument() throws SAXException {
        previousCharacters = false;
    }

    /**
    * Notify document end
    */

    public void endDocument() throws SAXException {
        previousCharacters = false;
    }    

    /**
    * Output an element start tag.
    * @params name The Name object naming the element. Use the getDisplayName() method
    * to obtain the tag to display in XML output.
    * @params attributes The attributes (excluding namespace declarations) associated with
    * this element. Note that the emitter is permitted to modify this list, e.g. to add
    * namespace declarations.
    */

    public void startElement(Name name, AttributeCollection attributes) throws SAXException {
        events.addElement(START_ELEMENT);
        events.addElement(name);

        // copy the attribute collection
        AttributeCollection atts;
        int numAtts = attributes.getLength();
        if (numAtts==0) {
            atts = emptyAttributeCollection;
        } else {
            atts = attributes.copy();
        }        
        events.addElement(atts);

        previousCharacters = false;        
    }

    /**
    * Output an element end tag
    * @params name The Name object naming the element. Use the getDisplayName() method
    * to obtain the tag to display in XML output.
    */

    public void endElement(Name name) throws SAXException {
        events.addElement(END_ELEMENT);
        events.addElement(name);
        previousCharacters = false;        
    }
        
    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        events.addElement(START_NAMESPACE);
        events.addElement(prefix);
        events.addElement(uri);
        previousCharacters = false;        
    }        
   
    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException {
        while (used + len >= buffer.length) {
            char[] newbuffer = new char[buffer.length * 2];
            System.arraycopy(buffer, 0, newbuffer, 0, used);
            buffer = newbuffer;
        }
        System.arraycopy(chars, start, buffer, used, len);
        if (previousCharacters) {
            // concatenate with the previous text node
            int[] v = (int[])events.elementAt(events.size()-1);
            v[1] += len;
        } else {      
            events.addElement(CHARACTERS);
            int[] val = {used, len};        // objects are expensive so we only create one
            events.addElement(val);  
        }
        used += len;
        previousCharacters = true;        
    }        

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String name, String data) throws SAXException {
        events.addElement(PROCESSING_INSTRUCTION);
        events.addElement(name);
        events.addElement(data);
        previousCharacters = false;        
    }
    
    /**
    * Output a comment. <br>
    * (The method signature is borrowed from the SAX2 LexicalHandler interface)
    */

    public void comment (char[] chars, int start, int length) throws SAXException{
        events.addElement(COMMENT);
        events.addElement(new String(chars, start, length));
        previousCharacters = false;        
    }

    /**
    * Set the Writer to be used. This has no effect when writing to
    * a result tree fragment.
    */

    public void setWriter (Writer writer) throws SAXException {}

    /**
    * Set the CharacterSet to be used. This has no effect when writing to
    * a result tree fragment.
    */

    public void setCharacterSet(CharacterSet charset) throws SAXException {}
    
    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. 
    */

    public void setEscaping(boolean escaping) throws SAXException {
        events.addElement((escaping ? ESCAPING_ON : ESCAPING_OFF));
        previousCharacters = false;
    }
    
    /**
    * Set output details. This has no effect when writing to
    * a result tree fragment.
    * @param details The details of the required output
    */

    public void setOutputDetails(OutputDetails details) throws SAXException {}


    /**
    * Get the root (document) node
    */

    public DocumentInfo getRootNode() throws SAXException {
        if (node!=null) {        // only do it once
            return (DocumentInfo)node;
        }
        Builder builder = new Builder();
        builder.setSystemId(baseURI);
        ContentHandlerProxy emitter = new ContentHandlerProxy();
        emitter.setUnderlyingContentHandler(builder);
        emitter.setRequireWellFormed(false);
        emitter.startDocument();
        replay(emitter);
        emitter.endDocument();
        node = builder.getCurrentDocument();
        return (DocumentInfo)node;
    }
        
    /**
    * Convert the result tree fragment to a node-set
    */

    //public NodeSetValue asNodeSet() throws SAXException {
    //    return new SingletonNodeSet(getRootNode());
    //}

    /**
    * Copy the result tree fragment value to a given Outputter
    */

    public void copy(Outputter out) throws SAXException {
        Emitter emitter = out.getEmitter();
        replay(emitter);
    }

    /**
    * Replay the saved emitter events to a new emitter
    */

    public void replay(Emitter emitter) throws SAXException {
        Enumeration enuma = events.elements();
        
        while (enuma.hasMoreElements()) {
            Object e = enuma.nextElement();
            Object e1;
            Object e2;
            if (e==START_ELEMENT) {
                e1 = enuma.nextElement();
                e2 = enuma.nextElement();
                emitter.startElement((Name)e1, (AttributeCollection)e2);
                
            } else if (e==END_ELEMENT) {
                e1 = enuma.nextElement();
                emitter.endElement((Name)e1);
                
            } else if (e==CHARACTERS) {
                e1 = enuma.nextElement();
                emitter.characters(buffer, ((int[])e1)[0], ((int[])e1)[1]);
                
            } else if (e==START_NAMESPACE) {
                e1 = enuma.nextElement();
                e2 = enuma.nextElement();                
                emitter.startPrefixMapping((String)e1, (String)e2);    
                                
            } else if (e==PROCESSING_INSTRUCTION) {
                e1 = enuma.nextElement();
                e2 = enuma.nextElement();
                emitter.processingInstruction((String)e1, (String)e2);
                
            } else if (e==COMMENT) {
                e1 = enuma.nextElement();
                emitter.comment(((String)e1).toCharArray(), 0, ((String)e1).length());

            } else if (e==ESCAPING_ON) {
                emitter.setEscaping(true);

            } else if (e==ESCAPING_OFF) {
                emitter.setEscaping(false);
                
            } else {
                throw new SAXException("Corrupt data in Result Tree Fragment: " + e);
            }
        }
        
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

