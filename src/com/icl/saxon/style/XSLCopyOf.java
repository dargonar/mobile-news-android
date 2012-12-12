package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.util.*;

/**
* An xsl:copy-of element in the stylesheet.<BR>
* The xsl:copy-of element takes:<ul>
* <li>an optional attribute select="pattern", defaulting to "." (the current element).</li>
* </ul>
*/

public class XSLCopyOf extends StyleElement {

    Expression select;

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

        String selectAtt = getAttributeValue("select");
        if (selectAtt!=null) {
            select = Expression.make(selectAtt, this);
        } else {
            reportAbsence("select");
        }
    }

    public void validate() throws SAXException {
        checkEmpty();
    }

    public void process( Context context ) throws SAXException
    {            
        Value value = select.evaluate(context);

        if (value instanceof FragmentValue) {
            ((FragmentValue)value).copy(context.getOutputter());
            
        } else if (value instanceof NodeSetValue) {
            ((NodeSetValue)value).sort();
            Vector v = ((NodeSetValue)value).getVector();
            for (int i=0; i<v.size(); i++) {
                NodeInfo node = (NodeInfo)v.elementAt(i);
                node.copy(context.getOutputter());
            }
            
        } else {
            context.getOutputter().writeContent(value.asString());
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
