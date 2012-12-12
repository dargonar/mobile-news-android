package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;

import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.util.*;

/**
* Handler for saxon:group elements in stylesheet. This is the same as xsl:for-each
* with the addition of a group-by attribute<BR>
*/

public class SAXONGroup extends XSLForEach {

    Expression groupBy = null;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {

        checkAllowedAttributes();
        
        String selectAtt = getAttributeValue("select");        
        if (selectAtt==null) {
            reportAbsence("select");
        } else {
            select = new NodeListExpression(Expression.make(selectAtt, this));
        }         
        String groupByAtt = getAttributeValue("group-by");
        if (groupByAtt == null) {
            reportAbsence("group-by");
        } else {
            groupBy = Expression.make(groupByAtt, this);
        }
    }

    protected void checkAllowedAttributes() throws SAXException {
        String[] allowed = {"select", "group-by"};
        allowAttributes(allowed);
    }


    public void validate() throws SAXException {
        checkWithinTemplate();
        select = handleSortKeys(select);  // handle sort keys if any: inherited from xsl:for-each

        // find the SAXONItem element
        NodeInfo n = this;
        SAXONItem item = null;
        while(n!=null) {
            if (n instanceof SAXONItem) {
                item = (SAXONItem)n;
                break;
            }
            n = n.getNextInDocument(this);
        }
        if (item==null) {
            throw new SAXException("saxon:group must have a nested saxon:item element");
        }
    }

    public void process( Context context ) throws SAXException
    {
        NodeSetValue nsv = (NodeSetValue)select.evaluateAsNodeSet(context);
        NodeEnumeration selection = nsv.enumerate();
        if (!(selection instanceof LastPositionFinder)) {
            selection = new LookaheadEnumerator(selection);
        }
        
        Context c = context.newContext();
        c.setLastPositionFinder((LastPositionFinder)selection);

        // create a new GroupActivation object and fire it off
            
        GroupActivation activation = new GroupActivation(this, groupBy, selection, c);
        Stack stack = c.getGroupActivationStack();
        stack.push(activation);

        while(activation.hasMoreElements()) {
            activation.nextElement();                
            processChildren(c);
        }
        
        stack.pop();
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
