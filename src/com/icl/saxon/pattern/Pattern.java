package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.expr.StaticContext;
import com.icl.saxon.expr.ExpressionParser;
import com.icl.saxon.expr.DummyStaticContext;
import com.icl.saxon.NameTest;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.Name;
import org.xml.sax.SAXException;


/**
* A Pattern represents the result of parsing an XSLT pattern string. <br>
* Patterns are created by calling the static method Pattern.make(string). <br>
* The pattern is used to test a particular node by calling match().
*/

public abstract class Pattern {

    protected StaticContext staticContext;

    /**
    * Static method to make a Pattern by parsing a String. <br>
    * @param pattern The pattern text as a String
    * @param env An object defining the compile-time context for the expression
    * @return The pattern object
    */

    public static Pattern make(String pattern, StaticContext env) throws SAXException {

        Pattern pat = (new ExpressionParser()).parsePattern(pattern, env).simplify(); 
        // previously used a shared parser instance: this wasn't thread-safe (bug 4.5/005)
        pat.staticContext = env;
        return pat;
    } 

    /**
    * Parse a pattern using a default compile-time context. This context does not support
    * the use of context-dependent facilities in expressions, including namespaces, use of
    * variables, or use of the document() function (which needs to know a base URI)
    * @param expression The expression (as a character string)
    * @param env An object giving information about the compile-time context of the expression
    * @return an object of type Expression
    */

    public static Pattern make(String pattern) throws SAXException {
        return make(pattern, new DummyStaticContext());
    }

    /**
    * Simplify the pattern by applying any context-independent optimisations.
    * Default implementation does nothing.
    * @return the optimised Pattern
    */

    public Pattern simplify() throws SAXException {
        return this;
    }

    /**
    * Set the static context used when the pattern was parsed
    */

    public final void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }

    /**
    * Determine the static context used when the pattern was parsed
    */

    public StaticContext getStaticContext() {
        return staticContext;
    }

    /**
    * Determine whether this Pattern matches the given Node
    * @param node The NodeInfo representing the Element or other node to be tested against the Pattern
    * @param context The context in which the match is to take place. Only relevant if the pattern
    * uses variables.
    * @return true if the node matches the Pattern, false otherwise
    */

    public abstract boolean matches(NodeInfo node, Context context) throws SAXException;

    /**
    * Determine the types of nodes to which this pattern applies. Used for optimisation.
    * For patterns that match nodes of several types, return NodeInfo.NODE
    * @return the type of node matched by this pattern. e.g. NodeInfo.ELEMENT or NodeInfo.TEXT
    */

    public int getType() {
        return NodeInfo.NODE;
    }

    /**
    * Determine the names of nodes to which this pattern applies. Used for
    * optimisation. 
    * @return A Name that the nodes must possess, or null
    * Otherwise return null.
    */

    public Name getName() {
        return null;
    }

    /**
    * Determine the names of nodes to which this pattern applies. Used for
    * optimisation. 
    * @return A NameTest that the nodes must satisfy, or null
    */

    public NameTest getNameTest() {
        return getName();
    }

    /**
    * Determine whether the pattern uses positional filters
    */

    public abstract boolean isRelative() throws SAXException;

    /**
    * Determine the default priority to use if this pattern appears as a match pattern
    * for a template with no explicit priority attribute.
    */

    public double getDefaultPriority() {
        return 0.5;
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
// The line marked PB-SYNC is by Peter Bryant (pbryant@bigfoot.com). All Rights Reserved. 
//
// Contributor(s): Michael Kay, Peter Bryant
//
