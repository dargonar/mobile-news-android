package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A NodeSetExpression is any expression denoting a set of nodes. <BR>
* This is an abstract class, the methods are defaults which may be overridden in subclasses
*/


public abstract class NodeSetExpression extends Expression {

    /**
    * Return a node enumeration. All NodeSetExpressions must implement this method:
    * the evaluate() function is defined in terms of it.
    */

    public abstract NodeEnumeration enumerate(Context context) throws SAXException;

    /**
    * Evaluate this node-set. This doesn't actually retrieve all the nodes: it returns a wrapper
    * around a node-set expression in which all context dependencies have been eliminated.
    */

    public final Value evaluate(Context context) throws SAXException {
        
        // lazy evaluation:
        // we eliminate all context dependencies, and save the resulting expression.
        
        Expression exp = reduce(Context.ALL_DEPENDENCIES, context);
        if (exp instanceof NodeSetValue) {
            return (Value)exp;
        } else if (exp instanceof NodeSetExpression) {
            return new NodeSetIntent((NodeSetExpression)exp);
        } else {
            Value value = exp.evaluate(context);
            if (value instanceof NodeSetValue) {
                return value;
            } else {
                throw new SAXException("Value must be a node-set: it is a " + exp.getClass());
            }
        }
    }

    /**
    * Return the first node selected by this Expression when evaluated in the current context
    * @param context The context for the evaluation
    * @return the NodeInfo of the first node in document order, or null if the node-set
    * is empty.
    */

    public NodeInfo selectFirst(Context context) throws SAXException {
        NodeEnumeration enuma = enumerate(context);
        if (enuma.isSorted()) {
            if (enuma.hasMoreElements()) {
                return enuma.nextElement();
            } else {
                return null;
            }
        } else {
            NodeInfo first = null;
            long minseq = Long.MAX_VALUE;
            while (enuma.hasMoreElements()) {
                NodeInfo next = enuma.nextElement();
                long seq = next.getSequenceNumber();
                if (seq < minseq) {
                    first = next;
                    minseq = seq;
                }
            }
            return first;
        }
    }

    /**
    * Evaluate as a string. Returns the string value of the first node
    * selected by the NodeSetExpression
    * @param context The context in which the expression is to be evaluated
    * @return the value of the NodeSetExpression, evaluated in the current context
    */

    public String evaluateAsString(Context context) throws SAXException {
        NodeInfo e = selectFirst(context);
        if (e==null) return "";
        return e.getValue();
    }

    /**
    * Evaluate as a boolean. Returns true if there are any nodes
    * selected by the NodeSetExpression
    * @param context The context in which the expression is to be evaluated
    * @return true if there are any nodes selected by the NodeSetExpression
    */

    public boolean evaluateAsBoolean(Context context) throws SAXException {
        return enumerate(context).hasMoreElements();
    }

    /**
    * Evaluate an expression as a NodeSet. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws SAXException {
        return (NodeSetValue)this.evaluate(context);
    }    

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
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
