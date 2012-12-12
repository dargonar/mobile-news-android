package com.icl.saxon.output;
import com.icl.saxon.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

/**
  * XHTMLEmitter is an Emitter that generates XHTML output.
  * It is the same as XMLEmitter except that it follows the legacy HTML browser
  * compatibility rules: for example, generating empty elements such as <BR />, and
  * using <p></p> for empty paragraphs rather than <p/>
  */
  
public class XHTMLEmitter extends XMLEmitter
{
    /**
    * Close an empty element tag.
    */

    protected String emptyElementTagCloser(String name) {
        if (HTMLEmitter.isEmptyTag(name)) {
            return " />";
        } else {
            return "></" + name + ">";
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
