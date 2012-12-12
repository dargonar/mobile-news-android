package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Stripper;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.util.*;

/**
* An xsl:preserve-space or xsl:strip-space elements in stylesheet.<BR>
*/

public class XSLPreserveSpace extends StyleElement {

    private boolean preserve;
    private String elements;

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"elements"};
        allowAttributes(allowed);

        preserve = (getLocalName().equals("preserve-space"));
        
        elements = attributeList.getValue("elements");
        if (elements==null)
            reportAbsence("elements");             
    }

    public void validate() throws SAXException {
        checkTopLevel();
    }

    public void preprocess() throws SAXException
    {
        // elements is a space-separated list of element names

        Stripper stripper = getPrincipalStyleSheet().getStripper();
        StringTokenizer st = new StringTokenizer(elements);
        while (st.hasMoreTokens()) {
            String s = st.nextToken().intern();
            if (s=="*") {
                stripper.setPreserveSpace(new AnyNameTest(), preserve);
            } else if (s.endsWith(":*")) {
                String prefix = s.substring(0, s.length()-2);
                PrefixTest test = new PrefixTest(getURIforPrefix(prefix));
                stripper.setPreserveSpace(test, preserve);
            } else {
                Name name = new Name(s, this, true);
                stripper.setPreserveSpace(name, preserve);
            }
        }
    }

    public void process(Context c) {}

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
