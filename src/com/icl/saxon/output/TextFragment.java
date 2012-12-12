package com.icl.saxon.output;
import java.io.*;
import org.xml.sax.*;

/**
* TextFragment is a subclass of OutputDetails; it is used when we want to send output
* to a StringWriter and retrieve it later.
*/

public class TextFragment extends OutputDetails {

    /**
    * Construct a default TextFragment object
    */
    
    public TextFragment() {
        writer = new StringWriter();
        setMethod("text");
        setEscaping(false);
        setIndent("no");
        setUserData("saxon:no-element-content");
    }

    /**
    * Retrieve the contents of the text that has been written
    */

    public String getText() {
        return writer.toString();
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
