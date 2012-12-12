package com.icl.saxon.style;
import com.icl.saxon.*;
import org.xml.sax.SAXException;


/**
* Handler for xsl:otherwise elements in stylesheet.<BR>
*/

public class XSLOtherwise extends StyleElement {

    public void prepareAttributes() throws SAXException {
        
        String[] allowed = {};
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        if (!(getParentNode() instanceof XSLChoose)) {
            throw styleError("xsl:otherwise must be immediately within xsl:choose");
        }
    }

    public void process( Context context ) throws SAXException
    {        
        processChildren(context);   // the condition is tested from the outer xsl:choose element
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
