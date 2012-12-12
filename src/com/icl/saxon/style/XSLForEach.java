package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import com.icl.saxon.trace.TraceListener;
import com.icl.saxon.om.NodeInfo;

/**
* Handler for xsl:for-each elements in stylesheet.<BR>
*/

public class XSLForEach extends StyleElement {

    Expression select = null;

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
        if (selectAtt==null) {
            reportAbsence("select");
        } else {
            select = Expression.make(selectAtt, this);
        }

    }

    public void validate() throws SAXException {
        checkWithinTemplate();
        select = handleSortKeys(select);
    }

    public void process( Context context ) throws SAXException
    {
        NodeSetValue nodes = (NodeSetValue)select.evaluateAsNodeSet(context);
        NodeEnumeration selection = nodes.enumerate();
        if (!(selection instanceof LastPositionFinder)) {
            selection = new LookaheadEnumerator(selection);
        }
        
        Context c = context.newContext();
        c.setLastPositionFinder((LastPositionFinder)selection);
        int position = 1;

        if (context.getController().isTracing()) {
            TraceListener listener = context.getController().getTraceListener();
            while(selection.hasMoreElements()) {
                NodeInfo node = selection.nextElement();
                c.setPosition(position++);
                c.setCurrentNode(node);
                c.setContextNode(node);
                listener.enterSource(null, c);
                processChildren(c);
                listener.leaveSource(null, c);
            }
        } else {
            while(selection.hasMoreElements()) {
                NodeInfo node = selection.nextElement();
                c.setPosition(position++);
                c.setCurrentNode(node);
                c.setContextNode(node);
                processChildren(c);
            }
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
