package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
* XMLIndenter: This ProxyEmitter indents elements, by adding character data where appropriate.
* The character data is always added as "ignorable white space", that is, it is never added
* adjacent to existing character data. 
*
* Author Michael Kay (mhkay@iclway.co.uk)
*/


public class XMLIndenter extends ProxyEmitter {

    private int level = 0;
    private int indentSpaces = 3;
    private String indentChars = "                                                          ";
    private boolean sameline = false;
    private boolean afterTag = true;      
    private boolean allWhite = true;

    /**
    * Start of document
    */

    public void startDocument() throws SAXException {
        super.startDocument();
        indentSpaces = outputDetails.getIndentSpaces();

        String omit = outputDetails.getOmitDeclaration();
        afterTag = omit==null || !omit.equals("yes") ||
                    outputDetails.getDoctypeSystem()!=null ;
    }

    /**
    * Output element start tag
    */

    public void startElement(Name tag, AttributeCollection atts) throws SAXException {
        if (afterTag) {
            indent();
        }
        super.startElement(tag, atts);
        level++;
        sameline = true;
        afterTag = true;
        allWhite = true;
    }

    /**
    * Output element end tag
    */
    
    public void endElement(Name tag) throws SAXException {
        level--;
        if (afterTag && !sameline) indent();        
        super.endElement(tag);
        sameline = false;
        afterTag = true;
        allWhite = true;
    }

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String target, String data) throws SAXException {
        super.processingInstruction(target, data);
        afterTag = true;
    }

    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException {
        for (int i=start; i<len; i++) {
            if (chars[i]=='\n') {
                sameline = false;
            }
            if (!Character.isWhitespace(chars[i])) {
                allWhite = false;
            }
        }
        super.characters(chars, start, len);
        if (!allWhite) {
            afterTag = false;
        }
    }

    /**
    * Output ignorable white space
    */

    public void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
        // ignore it
    }

    /**
    * Output a comment
    */

    public void comment(char[] chars, int start, int len) throws SAXException {
        super.comment(chars, start, len);
        afterTag = true;
    }

    /**
    * End of document
    */

    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
    * Output white space to reflect the current indentation level
    */

    private void indent() throws SAXException {
        int spaces = level * indentSpaces;
        while (spaces > indentChars.length()) {
            indentChars += indentChars;
        }
        char[] array = new char[spaces + 1];
        array[0] = '\n';
        indentChars.getChars(0, spaces, array, 1); 
        super.characters(array, 0, spaces+1);
        sameline = false;
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

