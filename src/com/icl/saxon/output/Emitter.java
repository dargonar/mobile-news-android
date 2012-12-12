package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import org.xml.sax.*;
import java.io.*;

/**
  * Emitter: This interface defines methods that must be implemented by
  * components that format SAXON output. There is one emitter for XML,
  * one for HTML, and so on. Additional methods are concerned with
  * setting options and providing a Writer.<p>
  *
  * The interface is deliberately designed to be as close as possible to the
  * standard SAX DocumentHandler interface, however, it allows additional
  * information to be made available.
  */
  
public interface Emitter 
{

    /**
    * Notify document start
    */

    public void startDocument() throws SAXException;

    /**
    * Notify document end
    */

    public void endDocument() throws SAXException;    

    /**
    * Output an element start tag.
    * @params name The Name object naming the element. Use the getDisplayName() method
    * to obtain the tag to display in XML output.
    * @params attributes The attributes (excluding namespace declarations) associated with
    * this element. Note that the emitter is permitted to modify this list, e.g. to add
    * namespace declarations.
    */

    public void startElement(Name name, AttributeCollection attributes) throws SAXException;

    /**
    * Output an element end tag
    * @params name The Name object naming the element. Use the getDisplayName() method
    * to obtain the tag to display in XML output.
    */

    public void endElement(Name name) throws SAXException;

    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call. Note that there is no
    * corresponding endPrefixMapping call: the Emitter is expected to keep track of
    * namespace nesting itself.
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException;

    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException;

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String name, String data) throws SAXException;
    
    /**
    * Output a comment. <br>
    * (The method signature is borrowed from the SAX2 LexicalHandler interface)
    */

    public void comment (char[] chars, int start, int length) throws SAXException;

    /**
    * Set the Writer to be used. The writer will already be set up to perform any encoding
    * requested. A writer will always be supplied before startDocument() is called.
    */

    public void setWriter (Writer writer) throws SAXException;

    /**
    * Set the CharacterSet to be used. The CharacterSet is a property of the encoding, it defines
    * which characters are available in the output encoding. If no character set is supplied,
    * the UnicodeCharacterSet should be assumed. It is the job of the emitter to provide a fallback
    * representation of characters that the Writer cannot handle.
    */

    public void setCharacterSet(CharacterSet charset) throws SAXException;
    
    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is also called at the start and end of a CDATA section
    * It is not called for other sections of output (e.g. comments) where escaping is inappropriate.
    */

    public void setEscaping(boolean escaping) throws SAXException;
    
    /**
    * Set output details. This supplies all the current output details to the emitter.
    * Most of these are derived directly from the xsl:output or saxon:output element in
    * the stylesheet.
    * @param details The details of the required output
    */

    public void setOutputDetails(OutputDetails details) throws SAXException;

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
