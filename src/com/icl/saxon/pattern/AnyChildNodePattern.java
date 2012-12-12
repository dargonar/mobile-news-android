package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
* An AnyChildNodePattern is the pattern node(), which matches any node except a root node,
* an attribute node, or a namespace node: in other words, any node that is the child of another
* node.
*/

public class AnyChildNodePattern extends Pattern {

    /**
    * Determine whether the pattern matches a given node. 
    * @param node the ElementInfo or other node to be tested
    * @return true if the pattern matches, else false
    */

    public boolean matches(NodeInfo node, Context c) throws SAXException {
        return (node instanceof ElementInfo ||
                node instanceof TextInfo ||
                node instanceof CommentInfo ||
                node instanceof ProcInstInfo);
    }

    /**
    * Determine the type of nodes to which this pattern applies. 
    * @return the node type
    */

    public int getType() {
        return NodeInfo.NODE;
    }
    
    /**
    * Determine the name of nodes to which this pattern applies.
    * @return null (meaning any name or no name)
    */

    public Name getName() {
        return null;
    }               

    /**
    * Return the pattern as a string
    */

    public String toString() {
        return "node()";
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
        return -0.5;
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
