package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* An expression whose value is always a set of nodes containing a single node, the document root.
*/

public class RootExpression extends SingletonExpression {


    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        return this;
    }

    /**
    * Return the first element selected by this Expression 
    * @param context The evaluation context
    * @return the NodeInfo of the first selected element, or null if no element
    * is selected
    */

    public NodeInfo getNode(Context context) throws SAXException {
        return context.getContextNode().getDocumentRoot();
    }

    /**
    * Evaluate as a string
    * @param context The context for evaluation
    * @return The concatenation of all the character data within the document
    */

    public String evaluateAsString(Context context) throws SAXException {
        return context.getContextNode().getDocumentRoot().getValue();
    }

    /**
    * Evaluate as a boolean. 
    * @param context The context (not used)
    * @return true (always - because the nodeset is never empty)
    */

    public boolean evaluateAsBoolean(Context context) throws SAXException {
        return true;
    }

    /**
    * Get a string representation of the expression
    */

    public String toString() {
        return "/";
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return Context.CONTEXT_NODE;    // because we need to know which document
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
            return new SingletonNodeSet(context.getContextNode().getDocumentRoot());
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
