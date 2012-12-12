package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ext.LexicalHandler;
import java.io.*;
import java.util.*;

/**
* A ContentHandlerProxy is an Emitter that filters data before passing it to an
* underlying SAX2 ContentHandler. Note that in general the output passed to an Emitter
* corresponds to an External General Parsed Entity. A SAX2 ContentHandler only expects
* to deal with well-formed XML documents, so we only pass it the contents of the first
* element encountered.
*/
  
public class ContentHandlerProxy implements Emitter
{
    protected ContentHandler handler;
    protected LexicalHandler lexicalHandler;
    protected Writer writer;
    protected CharacterSet characterSet;
    protected OutputDetails outputDetails;
    protected AttributesImpl attributes = new AttributesImpl();
    private int depth = 0;
    protected boolean requireWellFormed = true;

    // We keep track of namespaces to avoid outputting duplicate declarations. The namespaces
    // vector holds a list of all namespaces currently declared (organised as pairs of entries,
    // prefix followed by URI). The stack contains an entry for each element currently open; the
    // value on the stack is an Integer giving the size of the namespaces vector on entry to that
    // element.

    private String[] namespaces = new String[30];    // all namespaces currently declared
    private int namespacesSize = 0;                  // all namespaces currently declared
    private int[] namespaceStack = new int[100];
    private int nsStackTop = 0;
    private int nsCount = 0;                // number of namespaces for current element

    /**
    * Set the underlying content handler. This call is mandatory before using the Emitter.
    */

    public void setUnderlyingContentHandler(ContentHandler handler) {
        this.handler = handler;
        if (handler instanceof LexicalHandler) {
            this.lexicalHandler = (LexicalHandler)handler;
        }
    }

    /**
    * Set the Lexical Handler to be used. If called, this must be called AFTER
    * setUnderlyingContentHandler()
    */

    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
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
    * Indicate whether the content handler can handle a stream of events that is merely
    * well-balanced, or whether it can only handle a well-formed sequence.
    */

    public void setRequireWellFormed(boolean wellFormed) {
        requireWellFormed = wellFormed;
    }

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
        // System.err.println(this + " startDocument(), handler = " + handler);
        if (handler==null) {
            throw new SAXException("ContentHandlerProxy.startDocument(): no underlying handler provided");
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
    * will be notified before the relevant startElement call
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // Ignore declarations that are already present on the stack, provided they are
        // not masked by a different declaration of the same prefix

        for (int i=namespacesSize-2; i>=0; i-=2) {
            if (namespaces[i]==prefix) {
                if (namespaces[i+1]==uri) {
                    return;
                } else {
                    break;
                }
            }
        }       

        if (namespacesSize+2 >= namespaces.length) {
            String[] newlist = new String[namespacesSize*2];
            System.arraycopy(namespaces, 0, newlist, 0, namespacesSize);
            namespaces = newlist;
        }
        namespaces[namespacesSize++] = prefix;
        namespaces[namespacesSize++] = uri;
        nsCount++;
        handler.startPrefixMapping(prefix, uri);
    }        

    /**
    * Start of element
    */

    public void startElement(Name name, AttributeCollection atts) throws SAXException {
        depth++;
        if (depth>0) {
            // convert the AttributeCollection to a SAX2 Attributes object
            attributes.clear();
            for (int i=0; i<atts.getLength(); i++) {
                Name attname = atts.getExpandedName(i);
                attributes.addAttribute(
                    attname.getURI(),
                    attname.getLocalName(),
                    attname.getDisplayName(),
                    atts.getType(i),
                    atts.getValue(i));
            }

            handler.startElement(
                name.getURI(),
                name.getLocalName(),
                name.getDisplayName(),
                attributes);
        }

        // remember how many namespaces there were so we can unwind the stack later

        if (nsStackTop>=namespaceStack.length) {
            int[] newstack = new int[nsStackTop*2];
            System.arraycopy(namespaceStack, 0, newstack, 0, nsStackTop);
            namespaceStack = newstack;
        }

        namespaceStack[nsStackTop++] = nsCount;

        nsCount = 0;

    }

    /**
    * End of element
    */

    public void endElement(Name name) throws SAXException {
        if (depth>0) {
            handler.endElement(
                name.getURI(),
                name.getLocalName(),
                name.getDisplayName());
        }
        depth--;
        // if this was the outermost element, and well formed output is required
        // then no further elements will be processed
        if (requireWellFormed && depth<=0) {
            depth = Integer.MIN_VALUE;     // crude but effective
        }

        // discard the namespaces declared on this element
        
        if (nsStackTop-- == 0) {
            throw new SAXException("Attempt to output end tag with no matching start tag");
        }
        
        int nscount = namespaceStack[nsStackTop];
        for (int i=namespacesSize-2; i>=namespacesSize - (nscount*2); i-=2) {
            handler.endPrefixMapping(namespaces[i]);
        }
        namespacesSize -= (nscount*2);


    }

    /**
    * Character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException {
        if (depth>0 || !requireWellFormed) {
            handler.characters(chars, start, len);
        }
    }

    /**
    * Ignorable Whitespace
    */

    public void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
        if (depth>0 || !requireWellFormed) {        
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
    * Output a comment. Passes it on to the ContentHandler provided that the ContentHandler
    * is also a SAX2 LexicalHandler.
    */

    public void comment (char ch[], int start, int length) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.comment(ch, start, length);
        }
    }


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
