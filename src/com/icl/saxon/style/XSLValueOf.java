package com.icl.saxon.style;
import com.icl.saxon.*;

import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

/**
* An xsl:value-of element in the stylesheet.<BR>
* The xsl:value-of element takes attributes:<ul>
* <li>an mandatory attribute select="expression".
* This must be a valid String expression</li>
* <li>an optional disable-output-escaping attribute, value "yes" or "no"</li>
* </ul>
*/

public class XSLValueOf extends StyleElement {

    private Expression select;
    private boolean disable = false;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    public Expression getSelectExpression() {
        return select;
    }

    public void prepareAttributes() throws SAXException {

        if (getAttributeValue("default")!=null) {
            throw styleError("default attribute of xsl:value-of is no longer available");
        }

        String[] allowed = {"select", "disable-output-escaping"};
        allowAttributes(allowed);

        String selectAtt = getAttributeValue("select");
        if (selectAtt==null) {
            reportAbsence("select");
        }
        if (selectAtt.trim().equals(".")) {
            select = null;  // optimization
        } else {
            select = Expression.make(selectAtt, this);
        }

        String disableAtt = getAttributeValue("disable-output-escaping");
        if (disableAtt != null) {
            if (disableAtt.equals("yes")) {
                disable = true;
            } else if (disableAtt.equals("no")) {
                disable = false;
            } else {
                throw styleError("disable-output-escaping attribute must be either yes or no");
            }
        }


    }

    public void validate() throws SAXException {
        checkWithinTemplate();
        checkEmpty();
    }

    public void process( Context context ) throws SAXException
    {
        Outputter out = context.getOutputter();
        if (disable) out.setEscaping(false);
        
        if (select==null) {
            context.getCurrentNode().copyStringValue(out);
        } else {            
            String text = select.evaluateAsString(context);
            if (!text.equals("")) {
                out.writeContent(text);
            }
        }
        
        if (disable) out.setEscaping(true);        
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
