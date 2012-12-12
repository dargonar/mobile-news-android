package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import org.w3c.dom.Node;

/**
* A saxon:item element in the stylesheet. Iterates within a group provided the
* key value is the same as the previous item<BR>
*/

public class SAXONItem extends StyleElement {

    private SAXONGroup group;

    public void prepareAttributes() throws SAXException {
        String[] allowed = {};
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        checkWithinTemplate();
        Node parent = getParentNode();
        while (parent!=null) {
            if (parent instanceof SAXONGroup) {
                group = (SAXONGroup)parent;
                break;
            }
            parent = parent.getParentNode();
        }

        if (group==null) {
            throw styleError("saxon:item must be within a saxon:group");
        }

    }

    public void process( Context context ) throws SAXException
    {
        GroupActivation f = (GroupActivation)context.getGroupActivationStack().peek();
        
        while(true) {
            processChildren(context);
            if (!f.sameAsNext()) break;
            f.nextElement();
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
