package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

    /**
    * A ProxyEmitter is an Emitter that filters data before passing it to another
    * underlying Emitter.
    */
  
public abstract class ProxyEmitter implements Emitter
{
    protected Emitter baseEmitter;
    protected Writer writer;
    protected CharacterSet characterSet;
    protected OutputDetails outputDetails;
    
    /**
    * Set the underlying emitter. This call is mandatory before using the Emitter.
    */

    public void setUnderlyingEmitter(Emitter emitter) {
        baseEmitter = emitter;
    }

    /**
    * Set the Writer to be used. The writer will already be set up to perform any encoding
    * requested. A writer will always be supplied before startDocument() is called.
    */

    public void setWriter (Writer writer) throws SAXException {
        this.writer = writer;
        if (baseEmitter!=null)
            baseEmitter.setWriter(writer);
    }

    /**
    * Set the CharacterSet to be used. The CharacterSet is a property of the encoding, it defines
    * which characters are available in the output encoding. If no character set is supplied,
    * the UnicodeCharacterSet should be assumed.
    */

    public void setCharacterSet(CharacterSet charset) throws SAXException {
        this.characterSet = charset;
        if (baseEmitter!=null)
            baseEmitter.setCharacterSet(charset);
    }
    

    /**
    * Start of document
    */

    public void startDocument() throws SAXException {
        if (baseEmitter==null) {
            throw new SAXException("ProxyEmitter.startDocument(): no underlying emitter provided");
        }
        baseEmitter.startDocument();
    }

    /**
    * End of document
    */

    public void endDocument() throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.endDocument();
        }
    }

    /**
    * Start of element
    */

    public void startElement(Name name, AttributeCollection attributes) throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.startElement(name, attributes);
        }
    }

    /**
    * End of element
    */

    public void endElement(Name name) throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.endElement(name);
        }
    }

    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.startPrefixMapping(prefix, uri);
        }
    }        


    /**
    * Character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.characters(chars, start, len);
        }
    }


    /**
    * Processing Instruction
    */

    public void processingInstruction(String target, String data) throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.processingInstruction(target, data);
        }
    }

    /**
    * Output a comment
    */

    public void comment (char ch[], int start, int length) throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.comment(ch, start, length);
        }
    }


    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is not called for other sections of output (e.g.
    * element names) where escaping is inappropriate.
    */

    public void setEscaping(boolean escaping) throws SAXException {
        if (baseEmitter!=null) {
            baseEmitter.setEscaping(escaping);
        }
    }
    
    /**
    * Set the output details.
    */
    
    public void setOutputDetails (OutputDetails details) throws SAXException {
        outputDetails = details;        
        if (baseEmitter!=null) {
            baseEmitter.setOutputDetails(details);
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
