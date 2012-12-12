package com.icl.saxon.expr;
import com.icl.saxon.*;

import org.xml.sax.SAXException;
import java.util.*;

/**
* A FilterExpression contains a base expression and a filter predicate, which may be an
* integer expression (positional filter), or a boolean expression (qualifier)
*/

class FilterExpression extends NodeSetExpression {

    private Expression start;
    private Expression filter;

    /**
    * Constructor
    * @param start A node-set expression denoting the absolute or relative set of nodes from which the
    * navigation path should start.
    * @param filter An expression defining the filter predicate
    */

    public FilterExpression(Expression start, Expression filter) {
        this.start = start;
        this.filter = filter;
    }

    /**
    * Simplify an expression
    */

    public Expression simplify() throws SAXException {
        
        start = start.simplify();
        filter = filter.simplify();

        // ignore the filter if the base expression is an empty node-set
        if (start instanceof EmptyNodeSet) {
            return start;
        }

        // check whether the filter is a constant true() or false()
        if (filter instanceof Value && !(filter instanceof NumericValue)) {
            boolean f = ((Value)filter).asBoolean();
            if (f) {
                return start;
            } else {
                return new EmptyNodeSet();  
            }
        }
        return this;
    }

    /**
    * Evaluate the filter expression in a given context to return a Node Enumeration
    * @param context the evaluation context
    */

    public NodeEnumeration enumerate(Context context) throws SAXException {
        NodeEnumeration base = start.enumerate(context);
        if (!base.hasMoreElements()) {
            return base;        // quick exit for an empty node set
        }
        if ((filter.getDataType()==Value.NUMBER ||
             filter.getDataType()==Value.ANY ||
             filter.isRelative() )
                 && !base.isSorted()) {
            base = (new NodeSetExtent(base)).sort().enumerate();
        }
        return new FilterEnumerator(base, filter, context);
    }

    /**
    * Return a string representation of the expression
    * Used for diagnostics, and also for reconstituting the expression in the compiler
    */

    public String toString() {
        return start.toString() + "[" + filter.toString() + "]";
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        // not all dependencies in the filter expression matter, because the context node,
        // position, and size are not dependent on the outer context.
        return start.getDependencies() |
                (filter.getDependencies() & Context.XSLT_CONTEXT);
                    
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
        if ((dependencies & getDependencies()) != 0) {
            Expression newstart = start.reduce(dependencies, context);
            Expression newfilter = filter.reduce(dependencies & Context.XSLT_CONTEXT, context);
            Expression e = new FilterExpression(newstart, newfilter);
            e.setStaticContext(getStaticContext());
            return e.simplify();
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
