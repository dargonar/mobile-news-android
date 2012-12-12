package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import java.util.*;
import java.io.*;
import java.text.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
* CDATAFilter: This ProxyEmitter converts character data to CDATA sections,
* if the character data belongs to one of a set of element types to be handled this way.
*
* @author Michael Kay (Michael.Kay@icl.com)
*/


public class CDATAFilter extends ProxyEmitter {

    private StringBuffer buffer = new StringBuffer();
    private Stack stack = new Stack();
    private Hashtable nameList = new Hashtable();

    /**
    * Output element start tag
    */

    public void startElement(Name name, AttributeCollection atts) throws SAXException {
        flush(buffer);
        stack.push(name);
        super.startElement(name, atts);
    }

    /**
    * Output element end tag
    */
    
    public void endElement(Name tag) throws SAXException {
        flush(buffer);
        stack.pop();     
        super.endElement(tag);
    }

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String target, String data) throws SAXException {
        flush(buffer);
        super.processingInstruction(target, data);
    }

    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException {
        buffer.append(chars, start, len);
    }

    /**
    * Output ignorable white space
    */

    public void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
        buffer.append(chars, start, len);
    }

    /**
    * Output a comment
    */

    public void comment(char[] chars, int start, int len) throws SAXException {
        flush(buffer);
        super.comment(chars, start, len);
    }

    /**
    * Set escaping on or off
    */

    public void setEscaping(boolean escaping) throws SAXException {
        boolean cdata;
        if (stack.isEmpty()) {
            cdata = false;      // text is not part of any element
        } else {
            String name = ((Name)stack.peek()).getAbsoluteName();                        
            cdata = (nameList.get(name)!=null);
        }

        if (!cdata) {
            flush(buffer);
            super.setEscaping(escaping);
        }
    }

    /**
    * Flush the buffer containing accumulated character data,
    * generating it as CDATA where appropriate
    */

    public void flush(StringBuffer buffer) throws SAXException {
        boolean cdata;
        int end = buffer.length();
        if (end==0) return;
        
        if (stack.isEmpty()) {
            cdata = false;      // text is not part of any element
        } else {
            String name = ((Name)stack.peek()).getAbsoluteName();                        
            cdata = (nameList.get(name)!=null);
        }
        
        if (cdata) {

            // Check that the buffer doesn't include a character not available in the current
            // encoding

            int start = 0;
            int k = 0;
            while ( k<end ) {
                if (!characterSet.inCharset(buffer.charAt(k))) {

                    char[] array = new char[k-start];
                    buffer.getChars(start, k, array, 0);
                    flushCDATA(array, k-start);
                    
                    super.setEscaping(true);
                    char[] singleton = new char[1];
                    singleton[0] = buffer.charAt(k);
                    super.characters(singleton, 0, 1);
                    super.setEscaping(false);

                    start=k+1;       
                }
                k++;
            }
            char[] rest = new char[end-start];
            buffer.getChars(start, end, rest, 0);
            flushCDATA(rest, end-start);

        } else {
            char[] array = new char[end];
            buffer.getChars(0, end, array, 0);
            super.characters(array, 0, end);
        }

        buffer.setLength(0);

    }

    /**
    * Output an array as a CDATA section. At this stage we have checked that all the characters
    * are OK, but we haven't checked that there is no "]]>" sequence in the data
    */

    private void flushCDATA(char[] array, int len) throws SAXException {           
        super.setEscaping(false);
        super.characters(("<![CDATA[").toCharArray(), 0, 9);

        // Check that the character data doesn't include the substring "]]>" 

        int i=0;
        int doneto=0;
        while (i<len-2) {
            if (array[i]==']' && array[i+1]==']' && array[i+2]=='>') {
                super.characters(array, doneto, i+2-doneto);
                super.characters(("]]><![CDATA[").toCharArray(), 0, 12);
                doneto=i+2;
            }
            i++;
        }
        super.characters(array, doneto, len-doneto);
        super.characters(("]]>").toCharArray(), 0, 3);
        super.setEscaping(true);
    }

    /**
    * Set output details
    */

    public void setOutputDetails (OutputDetails details) throws SAXException
    {
        Vector elements = details.getCdataElements();
        for (int i=0; i<elements.size(); i++) {
            nameList.put(((Name)elements.elementAt(i)).getAbsoluteName(), "");
        }
        super.setOutputDetails(details);
    }

};

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

