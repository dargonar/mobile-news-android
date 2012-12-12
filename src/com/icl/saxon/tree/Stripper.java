package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.LexicalHandler;

import java.util.*;
import java.io.*;

/**
  * The Stripper class maintains details of which elements need to be stripped.
  * The code is written to act as a SAX filter to do the stripping.
  * It also includes a free-standing method to strip an existing tree.
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public class Stripper implements ContentHandler, LexicalHandler
{
    private final static Name XML_SPACE = Name.reconstruct("xml", Namespace.XML, "space");

    private Hashtable preserveElements;       // Elements that preserve / strip whitespace
    private Hashtable preservePrefixes;       // Prefixes that preserve / strip whitespace
    private boolean preserveByDefault;        // true if default is to preserve space
    private boolean preserveAll;              // true if all elements have whitespace preserved
    private ContentHandler nextHandler;
    
    // stripStack is used to hold information used while stripping nodes. We avoid allocating
    // space on the tree itself to keep the size of nodes down. Each entry on the stack is two
    // booleans, one indicates the current value of xml-space is "preserve", the other indicates
    // that we are in a space-preserving element.

    // We implement our own stack to avod the overhead of allocating objects. The two booleans
    // are held as the ls bits of a byte.
    
    private byte[] stripStack = new byte[100];
    private int top = 0;

    // buffer for accumulating character data, until the next markup event is received

    private char[] buffer = new char[4096];
    private int used = 0;

    /**
    * create a Stripper and initialise variables
    */

    public Stripper() {
        preserveByDefault = true;
        preserveAll = true;
        preserveElements = new Hashtable();
        preservePrefixes = new Hashtable();
        used = 0;
    }

    /**
    * Add an element to the list of elements for which white space is preserved / stripped.
    * @param name The element name
    * @param preserve true: preserve space for this element;<br>
    * false: strip space for this element
    */

    public void setPreserveSpace(Name name, boolean preserve) {
        preserveElements.put(name.getURI() + "^" + name.getLocalName(), new Boolean(preserve));
        if (!preserve) preserveAll = false;
    }

    public void setPreserveSpace(PrefixTest test, boolean preserve) {
        preservePrefixes.put(test.getURI(), new Boolean(preserve));
        if (!preserve) preserveAll = false;
    }

    public void setPreserveSpace(AnyNameTest any, boolean preserve) {
        preserveByDefault = preserve;
        if (!preserve) preserveAll = false;
    }
    
    /**
    * Decide whether an element is in the set of white-space preserving element types
    * @param uri The namespace URI of the element name
    * @param localname The local part of the element name
    * @return true if the element is in the set of white-space preserving element types
    */

    public boolean isSpacePreserving(String uri, String localname) {
        if (preserveAll) return true;
        Boolean b = (Boolean)preserveElements.get(uri + "^" + localname);
        if (b==null) {
            b = (Boolean)preservePrefixes.get(uri);
        }
        if (b!=null) return b.booleanValue();
        return preserveByDefault;
    }

    /**
    * Return true if all whitespace nodes are to be preserved
    */

    public boolean getPreserveAll() {
        return preserveAll;
    }

    // implement a SAX filter

    public void setNextHandler(ContentHandler handler) {
        nextHandler = handler;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startDocument () throws SAXException
    {
        top = 0;
        stripStack[top]=0x01;             // {xml:preserve = false, preserve this element = true}
        used = 0;
        nextHandler.startDocument();
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endDocument () throws SAXException 
    {
        flush();
        nextHandler.endDocument();
    }

    /**
    * Callback interface for SAX: not for application use
    */
    
    public void setDocumentLocator (Locator locator)
    {
        nextHandler.setDocumentLocator(locator);
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        nextHandler.startPrefixMapping(prefix, uri);
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endPrefixMapping(String prefix) throws SAXException {
        nextHandler.endPrefixMapping(prefix);
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startElement (String uri, String localname, String rawname, Attributes atts)
    throws SAXException
    {
        flush();
        nextHandler.startElement(uri, localname, rawname, atts);

        byte preserveParent = stripStack[top];
        
        String xmlspace = atts.getValue("xml:space");
        byte preserve = (byte)(preserveParent & 0x02);
        if (xmlspace!=null) {
            if (xmlspace.equals("preserve")) preserve = 0x02;
            if (xmlspace.equals("default")) preserve = 0x00;
        }
        if (isSpacePreserving(uri, localname)) {
            preserve |= 0x01;
        }

        // put "preserve" value on top of stack

        top++;
        if (top >= stripStack.length) {
            byte[] newStack = new byte[top*2];
            System.arraycopy(stripStack, 0, newStack, 0, top);
            stripStack = newStack;
        }
        stripStack[top] = preserve;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endElement (String uri, String localname, String rawname) throws SAXException
    {
        flush();
        nextHandler.endElement(uri, localname, rawname);
        top--;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void characters (char ch[], int start, int length) throws SAXException
    {
        // need to concatenate chunks of text before we can decide whether a node is all-white
        
        while (used + length > buffer.length) {
            char[] newbuffer = new char[buffer.length*2];
            System.arraycopy(buffer, 0, newbuffer, 0, used);
            buffer = newbuffer;
        }
        System.arraycopy(ch, start, buffer, used, length);
        used += length;
    }

    /**
    * Callback interface for SAX: not for application use
    */
 
    public void ignorableWhitespace (char ch[], int start, int length) throws SAXException
    {
        characters(ch, start, length);
    }
           
    /**
    * Callback interface for SAX: not for application use<BR>
    */

    public void processingInstruction (String name, String remainder) throws SAXException
    {
        flush();
        nextHandler.processingInstruction(name, remainder);
    }

    /**
    * Callback interface for SAX (part of LexicalHandler interface): not for application use
    */
 
    public void comment (char ch[], int start, int length) throws SAXException
    {
        flush();
        if (nextHandler instanceof LexicalHandler) {
            ((LexicalHandler)nextHandler).comment(ch, start, length);
        }
    }

    /**
    * Decide whether the accumulated character data is all whitespace
    */

    private boolean isWhite() {
        for (int i=0; i<used; i++) {
            if ( (int)buffer[i] > 0x20 ) {
                return false;
            }
        }
        return true;
    }

    /**
    * Flush buffer for accumulated character data, suppressing white space if appropriate
    */
    
    public void flush() throws SAXException {
        if (used > 0) {           
            if (stripStack[top]!=0 || !isWhite()) {
                nextHandler.characters(buffer, 0, used);
            }
            used = 0;
        }        
    }


    // No-op methods to satisfy lexical handler interface

    public void skippedEntity(String name) throws SAXException {}

    public void startDTD (String name, String publicId, String systemId)
	throws SAXException {
        if (nextHandler instanceof LexicalHandler) {
            ((LexicalHandler)nextHandler).startDTD(name, publicId, systemId);
        }
    }

    public void endDTD () throws SAXException {
        if (nextHandler instanceof LexicalHandler) {
            ((LexicalHandler)nextHandler).endDTD();
        }
    }

    public void startEntity (String name) throws SAXException {};

    public void endEntity (String name)	throws SAXException {};

    public void startCDATA () throws SAXException {};

    public void endCDATA ()	throws SAXException {};

    // Freestanding code to strip an existing tree

    /**
    * Prune the tree. This process strips all whitespace text nodes that need to be stripped.
    * NOTE: normally, when receiving input from a SAX event stream, whitespace nodes are stripped
    * from the event stream before they are added to the tree. There are some circumstances (e.g.
    * embedded stylesheets) where the document must be built before the stripping rules are known,
    * and this method is there to handle this situation.
    */

    public void strip(DocumentInfo doc) throws SAXException {
        if (!getPreserveAll()) {
            strip((ElementImpl)doc.getDocumentElement(), false);
        }
    }

    private void strip(ElementImpl parent, boolean preserve) throws SAXException {
        NodeInfo[] children = parent.getAllChildNodes();

        boolean preservingElement = isSpacePreserving(parent.getURI(), parent.getLocalName());            
        for (int i=0; i<children.length; i++) {
            NodeInfo child = children[i];
            if (child instanceof TextImpl) {
                TextImpl c = (TextImpl)child;
                if (!preservingElement && !preserve && c.isWhite()) {                             
                    c.removeNode();
                }
            } else if (child instanceof ElementImpl) {
                ElementImpl e = (ElementImpl)child;
                String xmlspace = e.getAttributeValue(XML_SPACE);
                boolean preserveX = preserve;
                if (xmlspace!=null) {
                    if (xmlspace.equals("preserve")) preserveX = true;
                    if (xmlspace.equals("default")) preserveX = false;
                }
                strip(e, preserveX);
            }
        }
        parent.renumberChildren();

    }


}   // end of outer class Stripper

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
