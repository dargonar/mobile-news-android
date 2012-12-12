package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.TextInfo;

import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

/**
* Handler for saxon:return elements in stylesheet.<BR>
* The saxon:return element has optional attribute select
*/

public class SAXONReturn extends XSLGeneralVariable {

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction 
    */

    public boolean isInstruction() {
        return true;
    }

    public void prepareAttributes() throws SAXException {
        
        String[] allowed = {"select"};
        allowAttributes(allowed);

        variableName = "#return-value#";

        String exprAtt = getAttributeValue("select");
        if (exprAtt!=null) {
            select = Expression.make(exprAtt, this);
        }
    }


    /**
    * Validate
    */

    public void validate() throws SAXException {

        // check it's within a function body

        NodeInfo anc = (NodeInfo)getParentNode();
        while (anc!=null) {
            if (anc instanceof SAXONFunction) break;
            if (anc instanceof XSLForEach ) {
                throw styleError("saxon:return may not be used within xsl:for-each");
            };
            if (anc instanceof SAXONGroup ) {
                throw styleError("saxon:return may not be used within saxon:group");
            };
            anc = (NodeInfo)anc.getParentNode();
        }

        if (anc==null) {
            throw styleError("saxon:return must only be used within saxon:function");
        }

        
        // check there is no following instruction

        NodeInfo next = (NodeInfo)getNextSibling();
        if (next!=null && !(next instanceof XSLFallback)) {
            throw styleError("saxon:return must be the last instruction in its template body");
        }

        if (select==null) {
            if (getNumberOfChildren()==0) {
                select = new StringValue("");
            }
        }          
    }

    /**
    * Process the return statement
    */

    public void process( Context context ) throws SAXException
    {
        Value value = getSelectValue(context);
        context.setReturnValue(value);
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
