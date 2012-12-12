package com.icl.saxon.pattern;
import com.icl.saxon.expr.*;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.axis.*;
import org.xml.sax.SAXException;
import java.util.Vector;

/**
* A LocationPathPattern represents a path, e.g. of the form A/B/C... The components are represented
* as a linked list, each component pointing to its predecessor
*/

public class LocationPathPattern extends Pattern {

    // the following public variables are exposed to the ExpressionParser
    
    public Pattern parentPattern = null;
    public Pattern ancestorPattern = null;
    public NameTest nameTest = new AnyNameTest();          
    public int nodeType = NodeInfo.NODE;
    protected Vector filters = new Vector(3);
    protected Expression extent = null;

    /**
    * Add a filter to the pattern (while under construction)
    * @param filter The predicate (a boolean expression or numeric expression) to be added
    */

    public void addFilter(Expression filter) {
        filters.addElement(filter);
    }

    /**
    * Simplify the pattern: perform any context-independent optimisations
    */

    public Pattern simplify() throws SAXException {

        // detect the simple cases: no parent or ancestor pattern, no predicates

        if (    parentPattern == null &&
                ancestorPattern == null &&
                filters.size() == 0) {
            if (nameTest instanceof AnyNameTest) {
                if (nodeType==NodeInfo.NODE) {
                    return new AnyChildNodePattern();
                }
                return new NodeTestPattern(nodeType);
            } else {
                return new NamedNodePattern(nodeType, nameTest);
            }
        }

        // simplify each component of the pattern

        if (parentPattern != null) parentPattern = parentPattern.simplify();
        if (ancestorPattern != null) ancestorPattern = ancestorPattern.simplify();
        for (int i=0; i<filters.size(); i++) {
            Expression filter = (Expression)filters.elementAt(i);
            filter = filter.simplify();
            if ((filter instanceof BooleanValue) && (((Value)filter).asBoolean())) {
                filters.removeElementAt(i);
                i--;
            } else {
                filters.setElementAt(filter, i);
            }
        }

        // see if it's an element pattern with a single positional predicate of [1]

        if (nodeType == NodeInfo.ELEMENT &&
                filters.size()==1 &&
                (filters.elementAt(0) instanceof NumericValue) &&
                (int)((NumericValue)filters.elementAt(0)).asNumber()==1 ) {
            FirstElementPattern fpat = new FirstElementPattern();            
            fpat.nameTest = nameTest;
            fpat.nodeType = nodeType;
            fpat.parentPattern = parentPattern;
            fpat.ancestorPattern = ancestorPattern;
            fpat.filters = filters;
            return fpat;
        }

        if (isRelative()) {
            makeExtent();
        }

        return this;
    }
    

    /**
    * For a positional pattern, make an equivalent nodeset expression to evaluate the filters
    */

    private void makeExtent() throws SAXException {
        Step step = new Step(Axis.CHILD, nodeType, nameTest);
        step.setFilters(filters);
        extent = new PathExpression(new ParentNodeExpression(), step);
    }

    /**
    * Determine whether the pattern matches a given node. 
    * @param node the ElementInfo or other node to be tested
    * @return true if the pattern matches, else false
    */

    // diagnostic version of method
    public boolean matchesX(NodeInfo node, Context context) throws SAXException {
        System.err.println("Matching node " + node + " against LP pattern " + this);
        boolean b = matchesX(node, context);
        System.err.println((b ? "matches" : "no match"));
        return b;
    }

    public boolean matches(NodeInfo node, Context context) throws SAXException {

        if (!node.isa(nodeType)) return false;
        if (!nameTest.isNameOf(node)) return false;
        
        if (parentPattern!=null) {
            NodeInfo par = (NodeInfo)node.getParentNode();
            if (par==null) return false;
            if (!(parentPattern.matches(par, context))) return false;
        }
        
        if (ancestorPattern!=null) {
            NodeInfo anc = (NodeInfo)node.getParentNode();
            while (true) {
                if (ancestorPattern.matches(anc, context)) break;
                anc = (NodeInfo)anc.getParentNode();
                if (anc==null) return false;
            }
        }
        
        if (filters.size()>0) {
            return testFilters(node, context);
        }
        
        return true;
    }

    /**
    * The testFilters() method is separated out for subclassing purposes
    */

    protected boolean testFilters(NodeInfo node, Context context) throws SAXException {

        Context c = context.newContext();
        c.setContextNode(node);
        c.setPosition(1);
        c.setLast(1);
        
        if (extent!=null) {
                
            // for a positional pattern, we do it the hard way: test whether the
            // node is a member of the nodeset obtained by evaluating the extent expression

            NodeSetValue nsv = (NodeSetValue)extent.evaluate(c);
            if (!nsv.contains(node)) return false;
        }
            
        else {
                
            for (int i=0; i<filters.size(); i++) {
                Expression filter = (Expression)filters.elementAt(i);
                if (!filter.evaluateAsBoolean(c)) return false;
            }
        }
            
        return true;

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
    * If this pattern will match only nodes of a single name, return the relevant name.
    * This is used for quick elimination of patterns that will never match.
    */

    public Name getName() {
        if (nameTest instanceof Name) return (Name)nameTest;
        return null;
    }               

    /**
    * Return the name test
    */

    public NameTest getNameTest() {
        return nameTest;
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
            case NodeInfo.TEXT:
                s = "text()";
                break;
            case NodeInfo.NODE:
                s = "node()";
                break;
            case NodeInfo.DOCUMENT:
                s = "/";
                break;
            case NodeInfo.COMMENT:
                s = "comment()";
                break;
            case NodeInfo.PI:
                s = "processing-instruction(\'" + nameTest.toString() + "\')";
                break;                
            default:
                s = "???";
        }
        
        for (int i=0; i<filters.size(); i++) {
            s += "[" + filters.elementAt(i).toString() + "]";
        }
        
        if (parentPattern != null) s = parentPattern.toString() + "/" + s; 
        if (ancestorPattern != null) s = ancestorPattern.toString() + "/" + s;

        return s;
            
    }


    /**
    * Determine if the pattern uses positional filters
    * @return true if there is a numeric filter in the pattern, or one that uses the position()
    * or last() functions
    */

    public boolean isRelative() throws SAXException {
        for (int i=0; i<filters.size(); i++) {
            Expression filter = (Expression)filters.elementAt(i);
            if (filter.isNumeric()) return true;
            if (filter instanceof VariableReference) return true;
            if (filter.isRelative()) return true;
        }
        return false;
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
