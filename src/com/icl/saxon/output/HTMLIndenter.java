package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.sort.HashMap;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
* HTMLIndenter: This ProxyEmitter indents HTML elements, by adding whitespace
* character data where appropriate.
* The character data is never added when within an inline element.
* The string used for indentation defaults to four spaces, but may be set using the
* indent-chars property
*
* Author Michael Kay (mhkay@iclway.co.uk)
*/


public class HTMLIndenter extends ProxyEmitter {

    private int level = 0;
    private int indentSpaces = 3;
    private String indentChars = "                                                          ";
    private boolean sameLine = false;
    private boolean isInlineTag = false;
    private boolean isFormattedTag = false;
    private boolean afterInline = false;
    private boolean afterFormatted = true;    // to prevent a newline at the start


    // the list of inline tags is from the HTML 4.0 (loose) spec. The significance is that we
    // mustn't add spaces immediately before or after one of these elements.

    private static String[] inlineTags = {
        "tt", "i", "b", "u", "s", "strike", "big", "small", "em", "strong", "dfn", "code", "samp",
         "kbd", "var", "cite", "abbr", "acronym", "a", "img", "applet", "object", "font",
         "basefont", "br", "script", "map", "q", "sub", "sup", "span", "bdo", "iframe", "input",
         "select", "textarea", "label", "button" };

    private static HashMap inlineTable = new HashMap(203);

    static {
        for (int j=0; j<inlineTags.length; j++) {
            inlineTable.set(inlineTags[j]);
        }
    }

    private static boolean isInline(String tag) {
        return inlineTable.get(tag);
    }

    // Table of preformatted elements

    private static HashMap formattedTable = new HashMap(51);

    static {
        formattedTable.set("pre");
        formattedTable.set("script");
        formattedTable.set("style");
        formattedTable.set("textarea");        
        formattedTable.set("xmp");          // obsolete but still encountered!
    }

    private static boolean isFormatted(String tag) {
        return formattedTable.get(tag);
    }    



    public HTMLIndenter() {}


    /**
    * Start of document
    */

    public void startDocument() throws SAXException {
        super.startDocument();
        indentSpaces = outputDetails.getIndentSpaces();
    }

    /**
    * Output element start tag
    */

    public void startElement(Name name, AttributeCollection atts) throws SAXException {
        String tag = name.getDisplayName();
        isInlineTag = isInline(tag);
        isFormattedTag = isFormatted(tag);
        if (!isInlineTag && !isFormattedTag && !afterInline && !afterFormatted) indent();
        isFormattedTag = isFormatted(tag);
        
        super.startElement(name, atts);
        level++;
        sameLine = true;
        afterInline = false;
        afterFormatted = false;
    }

    /**
    * Output element end tag
    */
    
    public void endElement(Name name) throws SAXException {
        level--;
        String tag = name.getDisplayName();
        boolean thisInline = isInline(tag);
        boolean thisFormatted = isFormatted(tag);
        if (!thisInline && !thisFormatted && !afterInline && !sameLine && !afterFormatted) {
            indent();
            afterInline = false;
            afterFormatted = false;
        } else {
            afterInline = thisInline;
            afterFormatted = thisFormatted;
        }
        super.endElement(name);
        isFormattedTag = false;
        sameLine = false;      
    }

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String target, String data) throws SAXException {
        super.processingInstruction(target, data);
    }

    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws SAXException {
        if (isFormattedTag) {
            super.characters(chars, start, len);
        } else {
            int lastNL = start;
        
            for (int i=start; i<start+len; i++) {
                if (chars[i]=='\n' || (i-lastNL > 120 && chars[i]==' ')) {
                    sameLine = false;
                    super.characters(chars, lastNL, i-lastNL);
                    indent();
                    lastNL = i+1;
                    while (lastNL<len && chars[lastNL]==' ') lastNL++;
                }
            }
            if (lastNL<start+len) {
                super.characters(chars, lastNL, start+len-lastNL);
            }
        }
        afterInline = false;
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
        indent();
        super.comment(chars, start, len);
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
        sameLine = false;
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

