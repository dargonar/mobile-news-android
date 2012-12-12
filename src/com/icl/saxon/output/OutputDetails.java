package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.w3c.dom.Node;

/**
* An OutputDetails object holds parameters controlling the generation of output files
*/

public class OutputDetails {

    Writer writer = null;
    OutputStream outputStream = null;
    Emitter emitter = null;
    
    String method = "saxon:uncommitted";
    String version = null;
    String indent = null;
    String encoding = "utf-8";
    String mediaType = null;
    String doctypeSystem = null;
    String doctypePublic = null;
    String omitDeclaration = "no";
    String standalone = null;
    String userData = null;
    Vector cdataElements = new Vector();
    PreparedStyleSheet nextInChain = null;
    boolean escaping = true;
    int indentSpaces = 3;
    boolean closeAfterUse = true;
    boolean includeHtmlMetaTag = true;
    Node node = null;

    /**
    * Construct a default OutputDetails object
    */
    
    public OutputDetails() {
    }

    /**
    * Construct an OutputDetails object as a copy of an existing OutputDetails object
    */

    public OutputDetails(OutputDetails base) {
        writer = base.writer;
        method = base.method;
        version = base.version;
        indent = base.indent;
        encoding = base.encoding;
        mediaType = base.mediaType;
        doctypeSystem = base.doctypeSystem;
        doctypePublic = base.doctypePublic;
        omitDeclaration = base.omitDeclaration;
        standalone = base.standalone;
        userData = base.userData;
        closeAfterUse = base.closeAfterUse;
        cdataElements = base.cdataElements;
        node = base.node;
        includeHtmlMetaTag = base.includeHtmlMetaTag;
    }

    /**
    * Set the writer to be used for producing output. If set, this overrides the
    * supplied output stream and encoding.
    */

    public final void setWriter(Writer w) {
        writer = w;
    }

    /**
    * Set the output stream to be used for producing output
    */

    public final void setOutputStream(OutputStream out) {
        outputStream = out;
    }

    /**
    * Set the method to be used
    * @param s the output method: "html", "xml", "text", "fop", or a user-supplied DocumentHandler
    * or Emitter class name
    */

    public final void setMethod(String s) {
        if (s!=null) {
            this.method = s.intern();
        }
    }

    /**
    * Set the version of the output format, e.g. "1.0" for xml or "4.0" for html
    */

    public final void setVersion(String s) {
        if (s!=null) {
            this.version = s.intern();
        }
    }

    /**
    * Set indenting on or off
    * @param s "yes" (indent) or "no" (don't indent)
    */

    public final void setIndent(String s) {
        if (s!=null) {
            this.indent = s.intern();
        }
    }

    /**
    * Set number of spaces to indent by
    */

    public void setIndentSpaces(int spaces) {
        indentSpaces = spaces;
    }

    /**
    * Set the encoding. This must be an encoding name recognised by the Java VM
    */

    public final void setEncoding(String s) {
        if (s!=null) {
            this.encoding = s.intern();
        }
    }

    /**
    * Set the MIME media type, e.g. "text/xml"
    */

    public final void setMediaType(String s) {
        if (s!=null) {
            this.mediaType = s.intern();
        }
    }

    /**
    * Set the System ID to be used in the DOCTYPE declaration
    */

    public final void setDoctypeSystem(String s) {
        if (s!=null) {
            this.doctypeSystem = s.intern();
        }
    }

    /**
    * Set the Public ID to be used in the DOCTYPE declaration
    */

    public final void setDoctypePublic(String s) {
        if (s!=null) {        
            this.doctypePublic = s.intern();
        }
    }

    /**
    * Indicate whether the XML Declaration should be omitted
    * @param s "yes": omit the declaration; "no": include the declaration
    */

    public final void setOmitDeclaration(String s) {
        if (s!=null) {
            this.omitDeclaration = s.intern();
        }
    }

    /**
    * Indicate whether the output document is "standalone"
    * @param s "yes" or "no"
    */

    public final void setStandalone(String s) {
        if (s!=null) {
            this.standalone = s.intern();
        }
    }

    /**
    * Set user data (from the saxon:output user-data attribute). This is only useful with
    * a user-defined Emitter.
    * @param s the user data
    */

    public final void setUserData(String s) {
        this.userData = s;
    }

    /**
    * Supply a list of output element names that are to be treated as CDATA elements
    * @param s A white-space separated list of element names
    */

    public final void addCdataElements(Vector v) {
        for (int i=0; i<v.size(); i++) {
            cdataElements.addElement(v.elementAt(i));
        }
    }    

    /**
    * Supply a single output element names that are to be treated as CDATA elements
    * @param s A white-space separated list of element names
    */

    public final void addCdataElement(Name name) {
        cdataElements.addElement(name);
    }    

    /**
    * Switch on or off escaping of special characters
    * @param b true: special characters are escaped; false: special characters are not escaped
    */

    public final void setEscaping(boolean b) {
        escaping = b;
    }

    /**
    * Set the Emitter to be used for output
    */

    public final void setEmitter(Emitter h) {
        emitter = h;
    }

    /**
    * Set the StyleSheet to be used to handle the output of this one
    */

    public final void setNextInChain(PreparedStyleSheet next) {
        nextInChain = next;
    }
    
    /**
    * Indicate whether the Writer should be closed after use
    */

    public final void setCloseAfterUse(boolean close) {
        closeAfterUse = close;
    }

    /**
    * Set the DOM node to which output should be appended
    */

    public final void setDOMNode(Node node) {
        this.node = node;
    }

    /**
    * Set whether a META tag should be included after the HEAD tag in HTML output
    */

    public void setIncludeHtmlMetaTag(boolean yes) {
        includeHtmlMetaTag = yes;
    }


    /**
    * Get the Writer being used for output
    */

    public final Writer getWriter() {
        return writer;
    }

    /**
    * Get the output stream being used for output
    */

    public final OutputStream getOutputStream() {
        return outputStream;
    }

    /**
    * Get the output method (html, xml, text etc)
    */

    public final String getMethod() {
        return this.method;
    }

    /**
    * Get the version of the output format, e.g. "1.0"
    */
       
    public final String getVersion() {
        return this.version;
    }

    /**
    * Determine whether indenting is on or off
    * @return "yes" or "no" as a string.
    */

    public final String getIndent() {
        return this.indent;
    }

    /**
    * Decide whether indenting is on or off
    * @return true or false
    */

    public final boolean isIndenting() {
        if (this.indent==null) return (this.method=="html" || this.method=="xhtml");
        return (this.indent == "yes");
    }

    /**
    * Get number of spaces to indent by
    */

    public int getIndentSpaces() {
        return indentSpaces;
    }

    /**
    * Get the character encoding
    */

    public final String getEncoding() {
        return this.encoding;
    }

    /**
    * Get the MIME media type. This will either be the media type specified, or if none
    * was specified, the media type associated with the output method; if that is unknown,
    * assume application/xml
    */

    public final String getMediaType() {
        if (mediaType==null) {
            if (method==null || method.equals("xml")) {
                return "application/xml";
            } else if (method.equals("html")) {
                return "text/html";
            } else if (method.equals("text")) {
                return "text/plain";
            } else {
                return "application/xml";
            }
        } else {
            return mediaType;
        }
    }

    /**
    * Get the System ID of the DOCTYPE declaration
    */

    public final String getDoctypeSystem() {
        return this.doctypeSystem;
    }

    /**
    * Get the Public ID of the DOCTYPE declaration
    */

    public final String getDoctypePublic() {
        return this.doctypePublic;
    }

    /**
    * Is XML Declaration to be omitted?
    * @return "yes" or "no"
    */

    public final String getOmitDeclaration() {
        return this.omitDeclaration;
    }

    /**
    * Is the output "standalone"?
    * @return "yes" or "no"
    */    

    public final String getStandalone() {
        return this.standalone;
    }

    /**
    * Get the user data (from the saxon:output user-data attribute)
    */

    public final String getUserData() {
        return this.userData;
    }

    /**
    * Get the list of elements to be treated as CDATA
    * @return a Vector of Name objects
    */

    public final Vector getCdataElements() {
        return cdataElements;
    }

    /**
    * Should special characters be escaped?
    */

    public final boolean isEscaping() {
        return escaping;
    }

    /**
    * Get the current Emitter
    */

    public final Emitter getEmitter() {
        return emitter;
    }

    /**
    * Get the StyleSheet to be used to handle the output of this one
    */

    public final PreparedStyleSheet getNextInChain() {
        return nextInChain;
    }
    
    /**
    * Determine whether the Writer should be closed after use
    */

    public final boolean getCloseAfterUse() {
        return closeAfterUse;
    }

    /**
    * Get the DOM node to which output should be attached
    */

    public final Node getDOMNode() {
        return node;
    }

    /**
    * Determine whether a META tag should be included after the HEAD tag in HTML output
    */

    public boolean getIncludeHtmlMetaTag() {
        return includeHtmlMetaTag;
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
