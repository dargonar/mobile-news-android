package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

/**
  * XMLEmitter is an Emitter that generates XML output
  * to a specified destination.
  */
  
public class XMLEmitter implements Emitter
{
    protected Writer writer = null;
    protected CharacterSet characterSet = null;
    protected OutputDetails outputDetails;

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

    protected boolean empty = true;
    protected boolean escaping = true;
    protected boolean openStartTag = false;
    protected boolean declarationIsWritten = false;

    static boolean[] specialInText;         // lookup table for special characters in text
    static boolean[] specialInAtt;          // lookup table for special characters in attributes
        // create look-up table for ASCII characters that need special treatment
    
    static {
        specialInText = new boolean[128];
        for (int i=0; i<=127; i++) specialInText[i] = false;
        specialInText['<'] = true;
        specialInText['>'] = true;
        specialInText['&'] = true;

        specialInAtt = new boolean[128];
        for (int i=0; i<=127; i++) specialInAtt[i] = false;
        specialInAtt['\n'] = true;
        specialInAtt['<'] = true;
        specialInAtt['>'] = true;
        specialInAtt['&'] = true;
        specialInAtt['\"'] = true;
    }

    /**
    * Set the Writer to use
    */

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
    * Set the character set to use
    */

    public void setCharacterSet(CharacterSet charSet) {
        characterSet = charSet;
    }

    /**
    * Set Document Locator. Provided merely to satisfy the interface.
    */

    public void setDocumentLocator(Locator locator) {}
    

    /**
    * Start of the document. Make the writer and write the XML declaration.
    */
    
    public void startDocument () throws SAXException 
    {
        if (characterSet==null) characterSet = new UnicodeCharacterSet();
        writeDeclaration();
        empty = true;
    }

    /**
    * Output the XML declaration
    */

    public void writeDeclaration() throws SAXException {
        if (declarationIsWritten) return;
        declarationIsWritten = true;
        try {

            String omit = outputDetails.getOmitDeclaration();
            if (omit==null) omit = "no";

            String version = outputDetails.getVersion();
            if (version==null) version = "1.0";

            String encoding = outputDetails.getEncoding();
            if (encoding==null) encoding = "UTF-8";

            if (!(encoding.equalsIgnoreCase("UTF-8")) && !(omit.equals("yes-really"))) omit = "no";

            String standalone = outputDetails.getStandalone();
            
            if (omit.equals("no")) {
                writer.write("<?xml version=\"" + version + "\" " +
                              "encoding=\"" + encoding + "\"" +
                              (standalone!=null ? (" standalone=\"" + standalone + "\"") : "") +
                              "?>");
                    // no longer write a newline character: it's wrong if the output is an
                    // external general parsed entity          
            }
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
    }


    /**
    * Output the document type declaration
    */

    protected void writeDocType(String type, String systemId, String publicId) throws SAXException {
        try {
            writer.write("\n<!DOCTYPE " + type + "\n");
            if (systemId!=null && publicId==null) {
                writer.write("  SYSTEM \"" + systemId + "\">\n");
            } else if (systemId==null && publicId!=null) {     // handles the HTML case
                writer.write("  PUBLIC \"" + publicId + "\">\n");
            } else {
                writer.write("  PUBLIC \"" + publicId + "\" \"" + systemId + "\">\n");
            }
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }    
    }

    /**
    * End of the document. Close the output stream.
    */
    
    public void endDocument () throws SAXException
    {
        try {
            if (outputDetails.getCloseAfterUse()) {
                //System.err.println("Close writer " + writer);
                writer.close();
            } else {
                writer.flush();
            }
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {

        // Ignore declarations that are already present on the stack, provided they are
        // not masked by a different declaration of the same prefix

        for (int i=namespacesSize-2; i>=0; i-=2) {
            if (namespaces[i].equals(prefix)) {
                if (namespaces[i+1].equals(uri)) {
                    return;
                } else {
                    break;
                }
            }
        }

        // Ignore xmlns="" unless there is already a different xmlns= declaration on the stack

        if (prefix.equals("") && uri.equals("")) {
            boolean found = false;
            for (int i=namespacesSize-2; i>=0; i-=2) {
                if (namespaces[i].equals("")) {
                    if (!namespaces[i+1].equals("")) {
                        found=true;
                    } 
                }
            }
            if (!found) return;
        }
            
        // Add the declarations to the stack, expanding it if necessary

        if (namespacesSize+2 >= namespaces.length) {
            String[] newlist = new String[namespacesSize*2];
            System.arraycopy(namespaces, 0, newlist, 0, namespacesSize);
            namespaces = newlist;
        }
        namespaces[namespacesSize++] = prefix;
        namespaces[namespacesSize++] = uri;
        nsCount++;
    }        

    /**
    * Start of an element. Output the start tag, escaping special characters.
    */
    
    public void startElement (Name fullname, AttributeCollection attributes) throws SAXException
    {
        // May need to output a namespace undeclaration (bug 5.3.2/014)
        if (fullname.getPrefix().equals("") && fullname.getURI().equals("")) {
            startPrefixMapping("", "");
        }
        
        String name = fullname.getDisplayName();
        try {
            if (empty) {
                String systemId = outputDetails.getDoctypeSystem();
                String publicId = outputDetails.getDoctypePublic();
                if (systemId!=null) {
                    writeDocType(name, systemId, publicId);
                }
                empty = false;
            }
            if (openStartTag) {
                closeStartTag(name, false);
            }
            writer.write('<');
            testCharacters(name);
            writer.write(name);

            // output the namespaces

            for (int n=namespacesSize - (nsCount*2); n<namespacesSize; n+=2) {
                writer.write(' ');
                String prefix = namespaces[n];
                String uri = namespaces[n+1];
                if (!(uri.equals(Namespace.XML))) {
                    if (prefix.equals("")) {
                        writeAttribute(name, "xmlns", "CDATA", uri);
                    } else {
                        writeAttribute(name, "xmlns:" + prefix, "CDATA", uri);
                    }
                }
            }
            
            // remember how many namespaces there were so we can unwind the stack later

            if (nsStackTop>=namespaceStack.length) {
                int[] newstack = new int[nsStackTop*2];
                System.arraycopy(namespaceStack, 0, newstack, 0, nsStackTop);
                namespaceStack = newstack;
            }
 
            namespaceStack[nsStackTop++] = nsCount;

            nsCount = 0;

            // output the attributes

            for (int i=0; i<attributes.getLength(); i++) {
                writer.write(' ');
                writeAttribute(
                    name,
                    attributes.getExpandedName(i).getDisplayName(),
                    attributes.getType(i),
                    attributes.getValue(i));
            }
            openStartTag = true;
            
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }            
    }

    protected void closeStartTag(String name, boolean emptyTag) throws SAXException {
        try {
            if (openStartTag) {
                if (emptyTag) {
                    writer.write(emptyElementTagCloser(name));
                } else {
                    writer.write('>');
                }
                openStartTag = false;
            }
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Close an empty element tag. (This is overridden in XHTMLEmitter).
    */

    protected String emptyElementTagCloser(String name) {
        return "/>";
    }
    
    /**
    * Write attribute name=value pair. The element name is not used in this version of the
    * method, but is used in the HTML subclass.
    */

    char[] attbuff1 = new char[256];
    protected void writeAttribute(String elname, String attname, String type, String value) throws SAXException {
        try {
            testCharacters(attname);
            writer.write(attname);
            writer.write("=\"");
            int len = value.length();

            if (len > attbuff1.length) {
                attbuff1 = new char[len];
            }
            value.getChars(0, len, attbuff1, 0);
            if (type=="NO-ESC") {                   // special type to indicate no escaping needed
                writer.write(attbuff1, 0, len);
            } else {
                writeEscape(attbuff1, 0, len, true);
            }
            writer.write('\"');        
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
    }


    /**
    * Test that all characters in a name are supported in the target encoding
    */

    protected void testCharacters(String name) throws SAXException {
        for (int i=name.length()-1; i>=0; i--) {
            if (!characterSet.inCharset(name.charAt(i))) {
                throw new SAXException("Invalid character in output name (" + name + ")");
            }
        }
    }

    protected void testCharacters(char[] array, int start, int len) throws SAXException {
        for (int i=start; i<len; i++) {
            if (!characterSet.inCharset(array[i])) {
                throw new SAXException("Invalid character in output ( &#" + (int)array[i] + "; )");
            }
        }
    }

    /**
    * End of an element.
    */

    public void endElement (Name name) throws SAXException
    {
        try {
            if (openStartTag) {
                closeStartTag(name.getDisplayName(), true);
            } else {
                writer.write("</");
                writer.write(name.getDisplayName());
                writer.write('>');
            }
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
        unwindNamespaces();
    }

    /**
    * Discard the namespaces declared on this element.
    * Separate from endElement() so it can be called from a subclass.
    */

    protected void unwindNamespaces() throws SAXException {
        if (nsStackTop-- == 0) {
            throw new SAXException("Attempt to output end tag with no matching start tag");
        }
        
        int nscount = namespaceStack[nsStackTop];
        namespacesSize -= (nscount*2);

    }


    /**
    * Character data.
    */

    public void characters (char[] ch, int start, int length) throws SAXException
    {   
        try {
            if (openStartTag) {
                closeStartTag("", false);
            }
            if (!escaping) {
                testCharacters(ch, start, length);
                writer.write(ch, start, length);
            } else {
                writeEscape(ch, start, length, false);
            }
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
    }


    /**
    * Handle a processing instruction.
    */
    
    public void processingInstruction (String target, String data)
        throws SAXException
    {
        try {
            if (openStartTag) {
                closeStartTag("", false);
            }
            writer.write("<?" + target + (data.length()>0 ? ' ' + data : "") + "?>");
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Write contents of array to current writer, after escaping special characters
    * @param ch The character array containing the string
    * @param start The start position of the input string within the character array
    * @param length The length of the input string within the character array
    * This method converts the XML special characters (such as < and &) into their
    * predefined entities. 
    */

    protected void writeEscape(char ch[], int start, int length, boolean inAttribute)
    throws java.io.IOException {
        int segstart = start;
        boolean[] specialChars = (inAttribute ? specialInAtt : specialInText);
        
        while (segstart < start+length) {
            int i = segstart;

            // find a maximal sequence of "ordinary" characters
            while (i < start+length &&
                     (ch[i]<128 ? !specialChars[ch[i]] : characterSet.inCharset(ch[i]))) {
                i++;
            }

            // write out this sequence
            writer.write(ch, segstart, i-segstart);

            // exit if this was the whole string
            if (i >= start+length) return;

            if (ch[i]>127) {

                // process characters not available in the current encoding
                
                int charval;

                //test for surrogate pairs
                //A surrogate pair is two consecutive Unicode characters.  The first
                //is in the range D800 to DBFF, the second is in the range DC00 to DFFF.
                //To compute the numeric value of the character corresponding to a surrogate
                //pair, use this formula (all numbers are hex):
        	    //(FirstChar - D800) * 400 + (SecondChar - DC00) + 10000

                if (ch[i]>=55296 && ch[i]<=56319) {
                    // we'll trust the data to be sound
                    charval = (((int)ch[i] - 55296) * 1024) + ((int)ch[i+1] - 56320) + 65536;
                    i++;
                } else {
                    charval = (int)ch[i];
                }

                outputCharacterReference(charval);

            } else {

                // process special ASCII characters

                if (ch[i]=='<') {
                    writer.write("&lt;");
                } else if (ch[i]=='>') {
                    writer.write("&gt;");
                } else if (ch[i]=='&') {
                    writer.write("&amp;");
                } else if (ch[i]=='\"') {
                    writer.write("&#34;");
                } else if (ch[i]=='\n') {
                    writer.write("&#xA;");
                }
            }
            segstart = ++i;
        }
    }

    private char[] charref = new char[10];
    protected void outputCharacterReference(int charval) throws java.io.IOException {
        
        int o = 0;
        charref[o++]='&';
        charref[o++]='#';
        String code = Integer.toString(charval);
        int len = code.length();
        for (int k=0; k<len; k++) {
            charref[o++]=code.charAt(k);
        }
        charref[o++]=';';
        writer.write(charref, 0, o);
    }

    /**
    * Set escaping on or off
    */

    public void setEscaping(boolean escaping) {
        this.escaping = escaping;
    }

    /**
    * Handle a comment.
    */
    
    public void comment (char ch[], int start, int length) throws SAXException
    {
        try {
            if (openStartTag) {
                closeStartTag("", false);
            }
            writer.write("<!--");
            writer.write(ch, start, length);
            writer.write("-->");
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Set output details
    */
    
    public void setOutputDetails (OutputDetails details) {
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
