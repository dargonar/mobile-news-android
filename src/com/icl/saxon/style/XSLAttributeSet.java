package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;

import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

/**
* An xsl:attribute-set element in the stylesheet.<BR>
*/

public class XSLAttributeSet extends StyleElement {

    Name fullname;  // the name of this attribute set, as a Name object
    String use;     // the value of the use-attribute-sets attribute, as supplied

    public String getAttributeSetName() {
        return fullname.getAbsoluteName();
    }

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"name", "use-attribute-sets"};
        allowAttributes(allowed);

        String name = attributeList.getValue("name");
        if (name==null)
            reportAbsence("name");
        fullname = new Name(name, this, false);
        
        use = attributeList.getValue("use-attribute-sets");

    }

    public void validate() throws SAXException {
        checkTopLevel();
        if (use!=null) {
            findAttributeSets(use);    // identify any attribute sets that this one refers to
        }
    }

    public void preprocess() throws SAXException {

        // do nothing until the attribute set is expanded
    }

    public void process(Context context) throws SAXException {

        // do nothing until the attribute set is expanded
    }

    public void expand(Context context) throws SAXException {
        processAttributeSets(context);
        processChildren(context);
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
