package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
* A pattern formed as the union (or) of two other patterns
*/

public class UnionPattern extends Pattern {

    protected Pattern p1, p2;
    private int nodeType = NodeInfo.NODE;

    /**
    * Constructor
    * @param p1 the left-hand operand
    * @param p2 the right-hand operand
    */

    public UnionPattern(Pattern p1, Pattern p2) {
        this.p1 = p1;
        this.p2 = p2;
        if (p1.getType()==p2.getType()) nodeType = p1.getType();
    }

    /**
    * Simplify the pattern: perform any context-independent optimisations
    */

    public Pattern simplify() throws SAXException {
        return new UnionPattern(p1.simplify(), p2.simplify());
    }

    /**
    * Determine if the supplied node matches the pattern
    * @e the node to be compared
    * @return true if the node matches either of the operand patterns
    */

    public boolean matches(NodeInfo e, Context c) throws SAXException {
        return p1.matches(e, c) || p2.matches(e, c);
    }

    /**
    * Determine the types of nodes to which this pattern applies. Used for optimisation.
    * For patterns that match nodes of several types, return Node.NODE
    * @return the type of node matched by this pattern. e.g. Node.ELEMENT or Node.TEXT
    */

    public int getType() {
        return nodeType;
    }

    /**
    * Get a string representation of the pattern
    */

    public String toString() {
        return "(" + p1.toString() + " | " + p2.toString() + ")";
    }

    /**
    * Determine if the pattern uses positional filters
    */

    public boolean isRelative() throws SAXException {
        return (p1.isRelative() || p2.isRelative());
    }

    /**
    * Get the LHS of the union
    */

    public Pattern getLHS() {
        return p1;
    }

    /**
    * Get the RHS of the union
    */

    public Pattern getRHS() {
        return p2;
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
