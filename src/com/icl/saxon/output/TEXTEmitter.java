package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import java.util.*;
import java.io.*;
import org.xml.sax.*;

/**
  * This class generates TEXT output
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public class TEXTEmitter extends XMLEmitter {

    // element content is output for xsl:output method="text", but is suppressed for text
    // output to attribute, comment, or processing-instruction nodes

    private int ignoreDepth = 0;
    private boolean ignoreElementContent = false;

    /**
    * Start of the document. 
    */
    
    public void startDocument () throws SAXException 
    {
        if (outputDetails.getMediaType()==null) {
            outputDetails.setMediaType("text/plain");
        }

        if ("saxon:no-element-content".equals(outputDetails.getUserData())) {
            ignoreElementContent = true;
        }

        if (characterSet==null) characterSet = new UnicodeCharacterSet();
        empty = true;
    }

    /**
    * Produce output using the current Writer. <BR>
    * Special characters are not escaped.
    * @param ch Character array to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception SAXException for any failure
    */

    public void characters(char ch[], int start, int length) throws SAXException {
        if (ignoreDepth == 0) {
            for (int i=start; i<start+length; i++) {
                if (!characterSet.inCharset(ch[i])) {
                    throw new SAXException("Output character not available in this encoding (decimal " + (int)ch[i] + ")");
                }
            }
            try {
                writer.write(ch, start, length);
            } catch (java.io.IOException err) {
                throw new SAXException(err);
            }
        }
    }

    /**
    * Output an element start tag. <br>
    * Does nothing with this output method.
    * @param name The element name (tag)
    */

    public void startElement(Name name, AttributeCollection atts) throws SAXException {
        if (ignoreElementContent) {
            ignoreDepth++;
        }
    }

    
    /**
    * Output an element end tag. <br>
    * Does nothing  with this output method.
    * @param name The element name (tag)
    */

    public void endElement(Name name) throws SAXException {
        if (ignoreElementContent) {
            ignoreDepth--;
        }
    }

    /**
    * Output a processing instruction. <br>
    * Does nothing  with this output method.
    */

    public void processingInstruction(String name, String value) throws SAXException {}

    /**
    * Output a comment. <br>
    * Does nothing with this output method.
    */

    public void comment(char ch[], int start, int length) throws SAXException {}

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
