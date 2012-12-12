package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.NameTest;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NodeInfo;
import org.xml.sax.SAXException;

/**
* A NamedNodePattern is a pattern that tests only for the node type and name.
* We optimise this as a special case.
*/

public class NamedNodePattern extends Pattern {

    private NameTest nameTest;
    private int nodeType;

    /**
    * Constructor.
    * @param nodeType A specific node type such as NodeInfo.ELEMENT or NodeInfo.ATTRIBUTE,
    * @param name The node name. Must not be null. Must be interned.
    * (For a match on node type only, use NodeTestPattern)
    */

    public NamedNodePattern(int nodeType, NameTest name) {
        this.nameTest = name;        
        this.nodeType = nodeType;
    } 
        
    /**
    * Determine whether the pattern matches a given node. 
    * @param node the ElementInfo or other node to be tested
    * @return true if the pattern matches, else false
    */

    public boolean matches(NodeInfo node, Context c) throws SAXException {
        return (node.isa(nodeType) && nameTest.isNameOf(node)); 
    }

    /**
    * Determine the type of nodes to which this pattern applies. 
    * @return the node type (e.g. NodeInfo.ELEMENT)
    */

    public int getType() {
        return nodeType;
    }
    
    /**
    * Return the name of nodes that this pattern will match.
    * This is used for quick elimination of patterns that will never match.
    */

    public Name getName() {
        if (nameTest instanceof Name) return (Name)nameTest;
        return null;
    }               

    /**
    * Return the pattern as a string
    */

    public String toString() {
        String s;
        switch (nodeType) {
            case NodeInfo.ATTRIBUTE:
                s = "@" + nameTest.toString();
                break;
            case NodeInfo.ELEMENT:
                s = nameTest.toString();
                break;
            default:
                s = "???";
        }

        return s;
            
    }

    /**
    * Determine if the pattern uses positional filters
    * @return false always
    */

    public boolean isRelative() {
        return false;
    }

    /**
    * Determine the default priority to use if this pattern appears as a match pattern
    * for a template with no explicit priority attribute.
    */

    public double getDefaultPriority() {
        return nameTest.getDefaultPriority();
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
