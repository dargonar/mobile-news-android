package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

/**
* A DocumentHandlerProxy is an Emitter that filters data before passing it to an
* underlying SAX DocumentHandler. Note that in general the output passed to an Emitter
* corresponds to an External General Parsed Entity. A SAX DocumentHandler only expects
* to deal with well-formed XML documents, so we only pass it the contents of the first
* element encountered.
*/
  
public class DocumentHandlerProxy implements Emitter
{
    protected DocumentHandler handler;
    protected Writer writer;
    protected CharacterSet characterSet;
    protected OutputDetails outputDetails;
    protected Vector namespaces = new Vector();
    private int depth = 0;

    /**
    * Set the underlying document handler. This call is mandatory before using the Emitter.
    */

    public void setUnderlyingDocumentHandler(DocumentHandler handler) {
        this.handler = handler;
    }

    /**
    * Set the Writer to be used. The writer will already be set up to perform any encoding
    * requested. A writer will always be supplied before startDocument() is called.
    */

    public void setWriter (Writer writer) throws SAXException {}

    /**
    * Set the CharacterSet to be used. The CharacterSet is a property of the encoding, it defines
    * which characters are available in the output encoding. If no character set is supplied,
    * the UnicodeCharacterSet should be assumed.
    */

    public void setCharacterSet(CharacterSet charset) throws SAXException {}
    

    /**
    * Set Document Locator
    */

    public void setDocumentLocator(Locator locator) {
        if (handler!=null)
            handler.setDocumentLocator(locator);
    }

    /**
    * Start of document
    */

    public void startDocument() throws SAXException {
        if (handler==null) {
            throw new SAXException("DocumentHandlerProxy.startDocument(): no underlying handler provided");
        }
        handler.startDocument();
        depth = 0;
    }

    /**
    * End of document
    */

    public void endDocument() throws SAXException {
        handler.endDocument();
    }

    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call. NOTE: in this Emitter, no
    * attempt is made to eliminate namespace prefixes already declared on an outer element.
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaces.addElement(prefix);
        namespaces.addElement(uri);
    }        

    /**
    * Start of element
    */

    public void startElement(Name name, AttributeCollection attributes) throws SAXException {
        depth++;
        if (depth>0) {
            for (int i=0; i<namespaces.size(); i+=2) {
                String prefix = (String)namespaces.elementAt(i);
                String uri = (String)namespaces.elementAt(i+1);
                if (prefix.equals("")) {
                    attributes.addAttribute(new Name("xmlns"), "NMTOKEN", uri);
                } else {
                    attributes.addAttribute(new Name("xmlns", "NS_URI", prefix), "NMTOKEN", uri);
                }
            }
            namespaces.removeAllElements();
            handler.startElement(name.getDisplayName(), attributes);
        }
    }

    /**
    * End of element
    */

    public void endElement(Name name) throws SAXException {
        if (depth>0) {
            handler.endElement(name.getDisplayName());
        }
        depth--;
        // if this was the outermost element, no further elements will be processed
        if (depth<=0) {
            depth = Integer.MIN_VALUE;     // crude but effective
        }
    }

    /**
    * Character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException {
        if (depth>0) {
            handler.characters(chars, start, len);
        }
    }

    /**
    * Ignorable Whitespace
    */

    public void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
        if (depth>0) {        
            handler.ignorableWhitespace(chars, start, len);
        }
    }

    /**
    * Processing Instruction
    */

    public void processingInstruction(String target, String data) throws SAXException {
        handler.processingInstruction(target, data);
    }

    /**
    * Output a comment
    */

    public void comment (char ch[], int start, int length) throws SAXException {}


    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is not called for other sections of output (e.g.
    * element names) where escaping is inappropriate.
    */

    public void setEscaping(boolean escaping) throws SAXException {}

    /**
    * Set indenting on or off
    */

    public void setIndenting(boolean indenting) {}

    /**
    * Set output details
    */

    public void setOutputDetails (OutputDetails details) throws SAXException
    {
        outputDetails = details;
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
