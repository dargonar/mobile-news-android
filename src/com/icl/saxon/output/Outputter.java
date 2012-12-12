package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import org.xml.sax.*;

/**
  * This class allows output to be generated. It channels output requests to an
  * Emitter which does the actual writing.
  * 
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A>
  * @version 19 April 2000: responsibility for eliminating duplicate namespaces has moved
  * to the Emitter.
  */

  // This class is really just a wrapper around the Emitter. It would be better if output
  // were done directly to the emitter, but this interface has been around for a while and
  // is widely used; and the class does add some logic, while Emitter is just an interface.
  

public class Outputter {

    private Emitter emitter;
    private OutputDetails outputDetails;
    
    private Name pendingStartTag = null;
    private AttributeCollection pendingAttList = new AttributeCollection(10);
    
    private String[] pendingNSList = new String[20];
    private int pendingNSListSize = 0;
   
    /**
    * Set the emitter that will deal with this output
    */

    public void setEmitter(Emitter handler) {
        this.emitter = handler;
    }

    /**
    * Get emitter. This is used by xsl:copy-of, a fragment is copied directly to the
    * Emitter rather than going via the Outputter.
    */
    
    public Emitter getEmitter() throws SAXException {
        if (pendingStartTag!=null) flushStartTag();
        return emitter;
    }

    public void setOutputDetails(OutputDetails details) throws SAXException {
        outputDetails = details;
        emitter.setOutputDetails(details);
    }

    public OutputDetails getOutputDetails() {
        return outputDetails;
    }

    /**
    * Switch escaping (of special characters) on or off.
    * @param escaping: true if special characters are to be escaped, false if not.
    */

    public void setEscaping(boolean escaping) throws SAXException {
        emitter.setEscaping(escaping);
    }

    /**
    * Start the output process
    */

    public void open() throws SAXException {
        //System.err.println("Open " + this + " using emitter " + emitter.getClass());
        emitter.startDocument();
    }

    /**
    * Produce literal output. This is written as is, without any escaping. 
    * The method is provided for Java applications that wish to output literal HTML text.
    * It is not used by the XSL system, which always writes using specific methods such as
    * writeStartTag().
    */

    char[] charbuffer = new char[1024];
    public void write(String s) throws SAXException {
        if (pendingStartTag!=null) flushStartTag();
        emitter.setEscaping(false);
        int len = s.length();
        if (len>charbuffer.length) {
            charbuffer = new char[len];
        }
        s.getChars(0, len, charbuffer, 0);
        emitter.characters(charbuffer, 0, len);
        emitter.setEscaping(true);
    }

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param s The String to be output
    * @exception SAXException for any failure
    */

    public void writeContent(String s) throws SAXException {
        if (s==null) return;
        int len = s.length();
        if (len>charbuffer.length) {
            charbuffer = new char[len];
        }
        s.getChars(0, len, charbuffer, 0);
        writeContent(charbuffer, 0, len);
    }
    
    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param chars Character array to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception SAXException for any failure
    */

    public void writeContent(char[] chars, int start, int length) throws SAXException {
        //System.err.println("WriteContent " + this + ":" + new String(chars, start, length) );
        if (length==0) return;
        if (pendingStartTag!=null) {
            flushStartTag();
        }
        emitter.characters(chars, start, length);
    }

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param chars StringBuffer containing to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception SAXException for any failure
    */

    public void writeContent(StringBuffer chars, int start, int length) throws SAXException {
        //System.err.println("WriteContent " + this + ":" + chars.substring(start, start+length) );
        if (length==0) return;
        if (pendingStartTag!=null) {
            flushStartTag();
        }
        char[] array = new char[length];
        chars.getChars(start, start+length, array, 0);
        emitter.characters(array, 0, length);
    }

    /**
    * Output an element start tag. <br>
    * The actual output of the tag is deferred until all attributes have been output
    * using writeAttribute(). 
    * @param name The element name
    */

    public void writeStartTag(Name name) throws SAXException {
        //System.err.println("Write start tag " + this + " : " + name + " to emitter " + emitter);
        if (pendingStartTag!=null) flushStartTag();
        pendingAttList.clear();
        pendingNSListSize = 0;
        pendingStartTag = name;
    }

    /**
    * Output a namespace declaration. <br>
    * This is added to a list of pending namespaces for the current start tag.
    * Note that unlike SAX2 startPrefixMapping(), this call is made AFTER writing the start tag.
    * @param prefix The namespace prefix ("" for the default namespace)
    * @param uri The namespace URI
    * @param substitute If true, when a duplicate prefix is issued for a different URI,
    * the prefix is replaced with a substitute prefix. If false, under these circumstances
    * the declaration is ignored as a duplicate.
    * @throws SAXException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeNamespaceDeclaration(String prefix, String uri, boolean substitute)
    throws SAXException {
        
        //System.err.println("Write namespace prefix=" + prefix + " uri=" + uri);
        if (pendingStartTag==null) {
            throw new SAXException("Cannot write a namespace declaration when there is no open start tag");
        }

        // elimination of namespaces already present on an outer element is now done by
        // the Emitter. 

        // Ignore declarations that are duplicated for this element type.
        // At the same time, look for the situation where several attributes
        // on the same element bind the same prefix to different
        // namespaces; in this case we must change the prefix

        for (int i=0; i<pendingNSListSize; i+=2) {
            if (pendingNSList[i].equals(prefix)) {
                if (pendingNSList[i+1].equals(uri) || !substitute) {
                    return;
                } else {
                    prefix = getSubstitutePrefix(prefix, uri);
                    break;
                }
            }
        }

        // if it's not a duplicate namespace, add it to the list for this start tag

        if (pendingNSListSize+2 > pendingNSList.length) {
            String[] newlist = new String[pendingNSListSize * 2];
            System.arraycopy(pendingNSList, 0, newlist, 0, pendingNSListSize);
            pendingNSList = newlist;
        }
        pendingNSList[pendingNSListSize++] = prefix;
        pendingNSList[pendingNSListSize++] = uri;

    }

    /**
    * It is possible for a single output element to use the same prefix to refer to different
    * namespaces. In this case we have to generate an alternative prefix for uniqueness. The
    * one we generate is based on a hashCode of the URI, which is almost certain to be unique.
    */

    private String getSubstitutePrefix(String prefix, String uri) {
        return prefix + "." + (uri.hashCode() & 0xffffff);
    }

    /**
    * Output an attribute value. <br>
    * This is added to a list of pending attributes for the current start tag, overwriting
    * any previous attribute with the same name. <br>
    * This method should NOT be used to output namespace declarations.
    * @param name The name of the attribute
    * @param value The value of the attribute
    * @throws SAXException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeAttribute(Name name, String value) throws SAXException {
        writeAttribute(name, value, false);
    }

    /**
    * Output an attribute value. <br>
    * This is added to a list of pending attributes for the current start tag, overwriting
    * any previous attribute with the same name. <br>
    * This method should NOT be used to output namespace declarations.
    * @param name The name of the attribute
    * @param value The value of the attribute
    * @param noEscape True if it's known there are no special characters in the value. If
    * unsure, set this to false.
    * @throws SAXException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeAttribute(Name name, String value, boolean noEscape) throws SAXException {
        //System.err.println("Write attribute " + name + "=" + value + " (" + noEscape + ")");
        if (pendingStartTag==null) {
            throw new SAXException("Cannot write an attribute when there is no open start tag");
        }
        
        String prefix = name.getPrefix();
        String uri = name.getURI();

        if (!prefix.equals("")) {
            
            // find the actual prefix used for this URI; it might have been changed to avoid
            // a conflict
            
            boolean found = false;
            while (!found) {
                for (int i=0; i<pendingNSListSize; i+=2) {
                    if (pendingNSList[i+1].equals(uri)) {
                        found = true;
                        String newPrefix = pendingNSList[i];
                        if (!prefix.equals(newPrefix)) {
                            name = new Name(newPrefix, uri, name.getLocalName());
                        }
                        break;
                    }
                }

                if (!found) {
                    // This can happen when copying an attribute node
                    writeNamespaceDeclaration(prefix, uri, true);
                    // and go round the loop again in case the same prefix is used twice
                }
            }
        }

        pendingAttList.setAttribute(name,
              (noEscape ? "NO-ESC" : "CDATA"),  // dummy attribute type to indicate no special chars
              value); 
    }


    /**
    * Output an element end tag.<br>
    * @param name The element name (tag)
    */

    public void writeEndTag(Name name) throws SAXException {
        //System.err.println("Write end tag " + this + " : " + name);
        if (pendingStartTag!=null) {
            flushStartTag();
        }
        
        // write the end tag
        emitter.endElement(name);
    }

    /**
    * Write a comment
    */

    public void writeComment(String comment) throws SAXException {
        if (pendingStartTag!=null) flushStartTag();
        emitter.comment(comment.toCharArray(), 0, comment.length());
    }

    /**
    * Write a processing instruction
    */

    public void writePI(String target, String data) throws SAXException {
        if (pendingStartTag!=null) flushStartTag();
        emitter.processingInstruction(target, data);
    }

    /**
    * Close the output
    */

    public void close() throws SAXException {
        //System.err.println("Close " + this + " using emitter " + emitter.getClass());
        emitter.endDocument();
    }

    /**
    * Flush out a pending start tag
    */

    protected void flushStartTag() throws SAXException {
        
        for (int i=0; i<pendingNSListSize; i+=2) {
            String prefix = pendingNSList[i];
            String uri = pendingNSList[i+1];
            emitter.startPrefixMapping(prefix, uri);
        }
        pendingNSListSize = 0;
            
        emitter.startElement(pendingStartTag, pendingAttList);
        pendingStartTag = null;
            
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
