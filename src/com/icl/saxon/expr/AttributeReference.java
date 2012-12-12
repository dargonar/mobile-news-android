package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* An expression that represents a reference to a named attribute
*/

class AttributeReference extends SingletonExpression {

    private Name name;
    private NodeInfo boundParentNode = null;    // null implies use the context node

    /**
    * Constructor
    */

    public AttributeReference(Name name) {
        this.name = name;
    }

    /**
    * Bind the reference to a particular node
    */

    public void bindParentNode(NodeInfo node) {
        boundParentNode = node;
    }

    /**
    * Get the parent node
    */

    private NodeInfo getParentNode(Context context) {
        if (boundParentNode == null) {
            return context.getContextNode();
        } else {
            return boundParentNode;
        }
    }

    /**
    * Return the relevant attribute node
    * @param context the evaluation context
    */

    public NodeInfo getNode(Context context) throws SAXException {
        NodeInfo node = getParentNode(context);
        if (node instanceof ElementInfo) {
            if (((ElementInfo)node).getAttributeValue(name)!=null) {
                return ((ElementInfo)node).makeAttributeNode(name);
            }
        }
        return null;
    }

    /**
    * Evaluate as a boolean. Returns true if there are any nodes
    * selected by the NodeSetExpression
    * @param context The context in which the expression is to be evaluated
    * @return true if there are any nodes selected by the NodeSetExpression
    */

    public boolean evaluateAsBoolean(Context context) throws SAXException {
        NodeInfo node = getParentNode(context);
        if (node instanceof ElementInfo) {
            return ((ElementInfo)node).getAttributeValue(name)!=null; 
        }
        return false;
    }

    /**
    * Evaluate as a string. Returns the string value of the attribute if it exists
    * @param context The context in which the expression is to be evaluated
    * @return true if there are any nodes selected by the NodeSetExpression
    */

    public String evaluateAsString(Context context) throws SAXException {
        NodeInfo node = getParentNode(context);
        if (node instanceof ElementInfo) {
            String s = ((ElementInfo)node).getAttributeValue(name);
            if (s==null) return "";
            return s;
        }
        return "";
    }

    /**
    * Return a string representation of the expression
    * Used for diagnostics
    */

    public String toString() {
        return "@" + name.getDisplayName();
    } 

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        if (boundParentNode==null) {
            return Context.CONTEXT_NODE;
        } else {
            return 0;
        }
    }

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed
    * @param context The context to be used for the partial evaluation
    * @return a new expression that does not have any of the specified
    * dependencies
    */

    public Expression reduce(int dependencies, Context context) throws SAXException {
        if (boundParentNode==null && ((dependencies & Context.CONTEXT_NODE) != 0)) {
            AttributeReference a = new AttributeReference(name);
            a.bindParentNode(context.getContextNode());
            a.setStaticContext(getStaticContext());
            return a;
        } else {
            return this;
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
