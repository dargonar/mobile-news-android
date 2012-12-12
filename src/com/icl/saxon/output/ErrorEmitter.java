package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;

import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.Writer;


/**
  * ErrorEmitter is an Emitter that generates an error message if any attempt
  * is made to produce output. It is used while a saxon:function is active to
  * prevent functions writing to the result tree.
  */
  
public class ErrorEmitter implements Emitter
{
    /**
    * Set the Writer to use. Provided merely to satisfy the interface.
    */

    public void setWriter(Writer writer) {}

    /**
    * Set the character set to use. Provided merely to satisfy the interface.
    */

    public void setCharacterSet(CharacterSet charSet) {}

    /**
    * Set Document Locator. Provided merely to satisfy the interface.
    */

    public void setDocumentLocator(Locator locator) {}   

    /**
    * Start of the document. 
    */
    
    public void startDocument () throws SAXException {}

    /**
    * End of the document. 
    */
    
    public void endDocument () throws SAXException {}

    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {}        

    /**
    * Start of an element. Output the start tag, escaping special characters.
    */
    
    public void startElement (Name fullname, AttributeCollection attributes) throws SAXException
    {
        error();
    }
    
    /**
    * End of an element.
    */

    public void endElement (Name name) throws SAXException
    {
        error();
    }


    /**
    * Character data.
    */

    public void characters (char[] ch, int start, int length) throws SAXException
    {   
        error();
    }


    /**
    * Handle a processing instruction.
    */
    
    public void processingInstruction (String target, String data)
        throws SAXException
    {
        error();
    }

    /**
    * Handle a comment.
    */
    
    public void comment (char ch[], int start, int length) throws SAXException
    {
        error();
    }

    /**
    * Report an error: can't write to result tree
    */

    private void error() throws SAXException {
        throw new SAXException("Cannot write to result tree while executing a function");
    }

    /**
    * Set output details
    */
    
    public void setOutputDetails (OutputDetails details) {}

    /**
    * Set escaping on or off: ignored in this Emitter
    */

    public void setEscaping(boolean escaping) {}

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
