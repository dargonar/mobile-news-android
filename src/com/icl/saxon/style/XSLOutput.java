package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;
import java.text.*;

/**
* An xsl:output element in the stylesheet. <BR>
* This is not to be confused with the xsl:output supported in earlier versions of SAXON,
* which was a precursor to the present saxon:output element. <br>
*/

public class XSLOutput extends StyleElement {

    String method = null;
    String version = null;
    String indent = null;
    String encoding = null;
    String mediaType = null;
    String doctypeSystem = null;
    String doctypePublic = null;
    String omitDeclaration = null;
    String standalone = null;
    Vector cdataElements = null;
    Emitter handler = null;

    public void prepareAttributes() throws SAXException {

        checkAllowedAttributes();

        method = getAttributeValue("method");
        if (method != null) {
            if (method.equals("xml") || method.equals("html") || method.equals("text"))  {
                // OK
            } else {
                Name methodName = new Name(method, this, false);
                if (methodName.getPrefix().equals("")) {
                    throw styleError("method must be xml, html, or text, or a prefixed name");
                }
                String localName = methodName.getLocalName();  // don't care what the prefix is
                
//                if (localName.equals("fop")) { 
//                    handler = new FOPEmitter();
//                    method="saxon:user";
//                } else 
                if (localName.equals("xhtml")) { 
                    method="xhtml";
                } else {
                    handler = makeEmitter(localName);
                    method="saxon:user";
                } 
            }
        }

        version = getAttributeValue("version");

        indent = getIndent();    // different for xsl:output and saxon:output

        encoding = getAttributeValue("encoding");

        mediaType = getAttributeValue("media-type");

        doctypeSystem = getAttributeValue("doctype-system");
        doctypePublic = getAttributeValue("doctype-public");

        omitDeclaration = getAttributeValue("omit-xml-declaration");
        if (omitDeclaration != null) {
            if (omitDeclaration.equals("yes") || omitDeclaration.equals("no")) {
                // OK
            } else {
                throw styleError("omit-xml-declaration attribute must be yes or no");
            }
        }

        standalone = getAttributeValue("standalone");
        if (standalone != null) {
            if (standalone.equals("yes") || standalone.equals("no")) {
                // OK
            } else {
                throw styleError("standalone attribute must be yes or no");
            }
        }
        
        String cdataAtt = getAttributeValue("cdata-section-elements");
        if (cdataAtt != null) {
            cdataElements = new Vector();
            StringTokenizer st = new StringTokenizer(cdataAtt);
            while (st.hasMoreTokens()) {
                String displayname = st.nextToken();
                Name fullname = new Name(displayname, this, true);
                cdataElements.addElement(fullname);
            }
        }
    }

    /**
    * Get indent value. 
    */

    protected String getIndent() throws SAXException {
        String att = getAttributeValue("indent");
        if (att==null || att.equals("yes") || att.equals("no")) return att;
        throw styleError("indent must be yes or no");
    }

    /**
    * Check that only the permitted attributes are present on this element.
    * This method is overridden in the subclass SAXONOutput, which allow additional attributes
    */

    protected void checkAllowedAttributes() throws SAXException {
        String[] allowed = {"method", "version", "indent", "encoding", "media-type",
            "doctype-system", "doctype-public", "omit-xml-declaration", "standalone",
             "cdata-section-elements"};
        allowAttributes(allowed);
    }        

    public void validate() throws SAXException {
        checkTopLevel();
        checkEmpty();
    }
    
    public void process( Context context ) throws SAXException {}

    public void setDetails(OutputDetails details) throws SAXException {
        if (method!=null) details.setMethod(method);
        if (version!=null) details.setVersion(version);
        if (indent!=null) details.setIndent(indent);
        if (encoding!=null) details.setEncoding(encoding);
        if (mediaType!=null) details.setMediaType(mediaType);
        if (doctypeSystem!=null) details.setDoctypeSystem(doctypeSystem);
        if (doctypePublic!=null) details.setDoctypePublic(doctypePublic);
        if (omitDeclaration!=null) details.setOmitDeclaration(omitDeclaration);
        if (standalone!=null) details.setStandalone(standalone);
        if (cdataElements!=null) details.addCdataElements(cdataElements);
        if (handler!=null) details.setEmitter(handler);
    }
    
    /**
    * load a named output emitter or document handler and check it is OK.
    */

    public static Emitter makeEmitter (String className) throws SAXException
    {

        Object handler = Loader.getInstance(className);            

        if (handler instanceof Emitter) {
            return (Emitter)handler;
        } else if (handler instanceof DocumentHandler) {
            DocumentHandlerProxy emitter = new DocumentHandlerProxy();
            emitter.setUnderlyingDocumentHandler((DocumentHandler)handler);
            return emitter;
        } else if (handler instanceof ContentHandler) {
            ContentHandlerProxy emitter = new ContentHandlerProxy();
            emitter.setUnderlyingContentHandler((ContentHandler)handler);
            return emitter;
        } else {
            throw new SAXException("Failed to load emitter " + className +
                        ": it is not a SAX DocumentHandler or SAX2 ContentHandler");
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
