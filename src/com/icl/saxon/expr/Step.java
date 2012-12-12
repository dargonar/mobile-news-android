package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.axis.*;
import com.icl.saxon.functions.*;

import org.xml.sax.SAXException;
import java.util.*;

/**
* A step in a path expression
*/

public final class Step {

    protected Axis axis;
    protected NameTest name;
    protected int nodeType;                       // NodeInfo.NODE selects nodes of any type

    private Vector filters = new Vector();      // list of filter expressions to apply

    public Step(int axisNumber, int nodeType, NameTest nodeName) throws SAXException {
        this.axis = Axis.make(axisNumber, nodeType, nodeName);
        this.name = nodeName;
        this.nodeType = nodeType;
    }

    public Step(Axis axis, int nodeType, NameTest nodeName) {
        this.axis = axis;
        this.name = nodeName;
        this.nodeType = nodeType;
    }

    public Step addFilter(Expression exp) {
        this.filters.addElement(exp);
        return this;
    }

    public void setFilters(Vector v) {
        this.filters = v;
    }

    public Axis getAxis() {
        return axis;
    }

    public int getNodeType() {
        return nodeType;
    }

    public NameTest getNodeName() {
        return name;
    }

    public Vector getFilters() {
        return filters;
    }

    /**
    * Simplify the step. Return either the same step after simplification, or null,
    * indicating that the step will always give an empty result.
    */

    public Step simplify() throws SAXException {

        // Check that the required node type can be found on this axis

        if (nodeType != NodeInfo.NODE) {
            if ((axis.getAxisNumber()==Axis.ATTRIBUTE) != (nodeType==NodeInfo.ATTRIBUTE)) {
                return null;
            } else if ((axis.getAxisNumber()==Axis.NAMESPACE) != (nodeType==NodeInfo.NAMESPACE)) {
                return null;
            }
        }
        
        for (int i=0; i<filters.size(); i++) {
            Expression exp = ((Expression)filters.elementAt(i)).simplify();

            // look for a filter that is constant true or false (which can arise after
            // an expression is reduced).
            
            if (exp instanceof Value && !(exp instanceof NumericValue)) {
                if (((Value)exp).asBoolean()) {         // filter is constant true
                    filters.removeElementAt(i);
                    i--;
                } else {                                // filter is constant false,
                                                        // so the wbole path-expression is empty
                    return null;
                }
            } else {                    
                filters.setElementAt(exp, i);
            }
        }
        return this;
    }

    /**
    * Enumerate this step.
    * @param node: The node from which we want to make the step
    * @param context: The context for evaluation. Affects the result of positional
    * filters
    * @return: an enumeration of nodes that result from applying this step
    */

    public NodeEnumeration enumerate(NodeInfo node, Context context)
        throws SAXException {

        NodeEnumeration enuma = axis.getEnumeration(node);
        if (enuma.hasMoreElements()) {       // if there are no nodes, there's nothing to filter
            for (int i=0; i<filters.size(); i++) {
                enuma = new FilterEnumerator(enuma, (Expression)filters.elementAt(i), context);
            }
        }
        return enuma;
                               
    }

    /**
    * Return a string representation of the Step
    */

    public String toString() {
        String s = axis.toString() + "::";
        switch (nodeType) {
        case NodeInfo.ATTRIBUTE:
            s += "@" + name.toString();
            break;
        case NodeInfo.ELEMENT:
            s += name.toString();
            break;
        case NodeInfo.TEXT:
            s += "text()";
            break;
        case NodeInfo.NODE:
            s += "node()";
            break;
        case NodeInfo.DOCUMENT:
            s += "/";
            break;
        default:
            s += "???";
        }
        
        for (int i=0; i<filters.size(); i++) {
            s += "[" + filters.elementAt(i).toString() + "]";
        }

        return s;
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
