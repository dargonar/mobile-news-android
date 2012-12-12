package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;

import org.xml.sax.SAXException;
import java.util.*;

/**
* Class ParentNodeExpression represents the XPath expression ".." or "parent::*"
*/

public class ParentNodeExpression extends SingletonExpression {

    /**
    * Return the node selected by this SingletonExpression
    * @param context The context for the evaluation
    * @return the parent of the current node defined by the context
    */

    public NodeInfo getNode(Context context) throws SAXException {
        return (NodeInfo)context.getContextNode().getParentNode();
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return Context.CONTEXT_NODE;
    }

    /**
    * Evaluate as a string. Returns the string value of the parent node
    * @param context The context in which the expression is to be evaluated
    * @return the value of the current node, identified by the context
    */

    public String evaluateAsString(Context context) throws SAXException {
        NodeInfo parent = (NodeInfo)context.getContextNode().getParentNode();
        if (parent==null) return "";
        return parent.getValue();
    }

    /**
    * Evaluate as a boolean. Returns true if there are any nodes
    * selected by the NodeSetExpression. 
    * @param context The context in which the expression is to be evaluated 
    * @return true unless the current node is the Document node
    */

    public boolean evaluateAsBoolean(Context context) throws SAXException {
        return (context.getContextNode().getParentNode()!=null);
    }

    /**
    * Get a string representation of this expression
    */

    public String toString() {
        return "..";
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
        if ((dependencies & Context.CONTEXT_NODE) != 0 ) {
            return new SingletonNodeSet((NodeInfo)context.getContextNode().getParentNode());
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
