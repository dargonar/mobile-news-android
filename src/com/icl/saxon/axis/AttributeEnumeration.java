package com.icl.saxon.axis;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.ElementInfo;
import com.icl.saxon.om.Name;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* AttributeEnumeration is an enumeration of all the attribute nodes of an Element.
*/

public class AttributeEnumeration implements NodeEnumeration {
    
    private ElementInfo element;
    private AttributeCollection attlist;
    private NameTest nodeName;
    private int index;
    private int length;

    /**
    * Constructor
    * @param node: the element whose attributes are required. This may be any type of node,
    * but if it is not an element the enumeration will be empty
    * @param nodeType: the type of node required. This may be any type of node,
    * but if it is not an attribute the enumeration will be empty
    * @param nameTest: condition to be applied to the names of the attributes selected
    */

    public AttributeEnumeration(NodeInfo node, int nodeType, NameTest nodeName)
    throws SAXException {

        this.nodeName = nodeName;
        
        if (node instanceof ElementInfo &&
                (nodeType==NodeInfo.ATTRIBUTE || nodeType==NodeInfo.NODE)) {
            element = (ElementInfo)node;
            if (nodeName instanceof Name) {
                String attributeValue = element.getAttributeValue((Name)nodeName);                    
                index = 0;      // ensure first call only succeeds
                length = (attributeValue==null ? 0 : 1);                    
            } else {                    
                attlist = element.getAttributeList();
                index = 0;
                length = attlist.getLength();
            }
        }
        else {      // if it's not an element, or if we're not looking for attributes,
                    // then there's nothing to find
            index = 0;
            length = 0;
        }
    }

    /**
    * Test if there are mode nodes still to come.
    * ("elements" is used here in the sense of the Java enumeration class, not in the XML sense)
    */

    public boolean hasMoreElements() throws SAXException {
        return index < length;
    }

    /**
    * Get the next node in the enumeration.
    * ("elements" is used here in the sense of the Java enumeration class, not in the XML sense)
    */

    public NodeInfo nextElement() throws SAXException {
        if (nodeName instanceof Name) {
            index++;
            return element.makeAttributeNode((Name)nodeName);
        } else {   
            return element.makeAttributeNode(element.getAttributeList().getExpandedName(index++));
        }
    }

    public boolean isSorted() {
        return true;            // in the sense that there is no need to sort them again
    }

    public boolean isReverseSorted() {
        return false;
    }

    public boolean isPeer() {
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
