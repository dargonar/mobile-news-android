package com.icl.saxon.output;

import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import java.util.*;
import java.io.Writer;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

/**
  * This class generates XML or HTML output depending on whether the first tag output is "<html>"
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public class UncommittedEmitter extends ProxyEmitter {

    boolean committed = false;
    boolean initialNewline = false;
    boolean initialEscaping = true;
    StringBuffer pendingCharacters;
    Vector pendingNamespaces;

    public void startDocument() throws SAXException {
        committed = false;
    }

    /**
    * End of document
    */

    public void endDocument() throws SAXException {
        // empty output: must send a beginDocument()/endDocument() pair to the content handler
        if (!committed) {
            switchToXML();
        }
        super.endDocument();
    }

    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (!committed) {
            if (pendingNamespaces==null) {
                pendingNamespaces = new Vector();
            }
            pendingNamespaces.addElement(prefix);
            pendingNamespaces.addElement(uri);
        } else {
            super.startPrefixMapping(prefix, uri);
        }
    }      

    /**
    * Produce character output using the current Writer. <BR>
    */

    public void characters(char ch[], int start, int length) throws SAXException {
        if (!committed) {
            boolean allWhite = true;
            if (pendingCharacters==null) {
                pendingCharacters=new StringBuffer();
            }
            for (int i=start; i<start+length; i++) {
                char c = ch[i];
                if (!Character.isWhitespace(c)) {
                    allWhite = false;
                }
                if (initialEscaping) {
                    if (c=='<') {
                        pendingCharacters.append("&lt;");
                    } else if (c=='>') {
                        pendingCharacters.append("&gt;");
                    } else if (c=='&') {
                        pendingCharacters.append("&amp;");
                    } else {
                        pendingCharacters.append(c);
                    }
                } else {
                    pendingCharacters.append(c);
                }
            }
            if (!allWhite) {
                switchToXML();
            }
        } else {
            super.characters(ch, start, length);
        }
    }
    
    /**
    * Processing Instruction
    */

    public void processingInstruction(String target, String data) throws SAXException {
        if (!committed) {
            if (pendingCharacters==null) {
                pendingCharacters=new StringBuffer();
            }
            pendingCharacters.append("<?" + target + " " + data + "?>");
        } else {
            super.processingInstruction(target, data);
        }
    }

    /**
    * Output a comment
    */

    public void comment (char ch[], int start, int length) throws SAXException {
        if (!committed) {
            if (pendingCharacters==null) {
                pendingCharacters=new StringBuffer();
            }
            pendingCharacters.append("<!--" + new String(ch, start, length) + "-->");
        } else {
            super.comment(ch, start, length);
        }
    }

    /**
    * Output an element start tag. <br>
    * This can only be called once: it switches to a substitute output generator for XML or HTML,
    * depending on whether the tag is "HTML".
    * @param name The element name (tag)
    */

    public void startElement(Name fullname, AttributeCollection attributes) throws SAXException {
        if (!committed) {
            String name = fullname.getLocalName();
            String uri = fullname.getURI();
            if (name.equalsIgnoreCase("html") && uri=="") {
                switchToHTML();
            } else {
                switchToXML();
            }
        }
        super.startElement(fullname, attributes);
    }

    /**
    * Switch to an XML emitter
    */

    private void switchToXML() throws SAXException {
        Emitter e = new XMLEmitter();
        String indent = outputDetails.getIndent();
        if (indent!=null && indent.equals("yes")) {
            XMLIndenter in = new XMLIndenter();
            in.setUnderlyingEmitter(e);
            e = in;
        }        
        if (outputDetails.getCdataElements().size()>0) {
            CDATAFilter filter = new CDATAFilter();
            filter.setUnderlyingEmitter(e);
            e=filter;
        }
        switchTo(e);
    }

    /**
    * Switch to an HTML emitter
    */

    private void switchToHTML() throws SAXException {
        Emitter e = new HTMLEmitter();
        String indent = outputDetails.getIndent();
        if (indent==null || indent.equals("yes")) {
            HTMLIndenter in = new HTMLIndenter();
            in.setUnderlyingEmitter(e);
            e = in;
        }        
        switchTo(e);
    }

    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is not called for other sections of output (e.g.
    * element names) where escaping is inappropriate.
    */

    public void setEscaping(boolean escaping) throws SAXException {
        if (!committed) {
            initialEscaping = escaping;
        }
        super.setEscaping(escaping);
    }
        
    /**
    * Switch to a new underlying emitter
    */

    private void switchTo(Emitter emitter) throws SAXException {
        setUnderlyingEmitter(emitter);
        committed = true;
        emitter.setWriter(writer);
        emitter.setCharacterSet(characterSet);
        emitter.setOutputDetails(outputDetails);
        emitter.startDocument();
        if (pendingCharacters!=null) {
            emitter.setEscaping(false);
            int len = pendingCharacters.length();
            char[] chars = new char[len];
            pendingCharacters.getChars(0, len, chars, 0);
            emitter.characters(chars, 0, len);
        }
        if (pendingNamespaces!=null) {
            for (int n=0; n<pendingNamespaces.size(); n+=2) {
                emitter.startPrefixMapping((String)pendingNamespaces.elementAt(n),
                                            (String)pendingNamespaces.elementAt(n+1));
            }
        }
        emitter.setEscaping(initialEscaping);
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
