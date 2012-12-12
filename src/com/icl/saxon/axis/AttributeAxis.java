package com.icl.saxon.axis;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

class AttributeAxis extends Axis {

    public NodeEnumeration getEnumeration(NodeInfo node)
        throws SAXException {
        return new AttributeEnumeration(node, nodeType, nodeName);
    }

    /**
    * Get the principal node type for the axis. This is the default node type, and is
    * Node.ELEMENT for all axes except the Attribute and Namespace axes
    */

    public int getPrincipalNodeType() {
        return NodeInfo.ATTRIBUTE;
    }

    public String toString() {
        return "attribute";
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isSibling() {
        return false;
    }

    public boolean isPeer() {
        return true;
    }

    public boolean isWithinSubtree() {
        return true;
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
