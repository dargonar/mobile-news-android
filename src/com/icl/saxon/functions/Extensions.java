package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.*;
import java.util.*;
import java.text.*;

/**
* This class implements functions that are supplied as standard with SAXON,
* but which are not defined in the XSLT or XPath specifications. <p>
*
* To invoke these functions, use a function call of the form prefix:name() where
* name is the method name, and prefix maps to a URI such as
* http://icl.com/saxon/com.icl.saxon.functions.Extensions (only the part
* of the URI after the last slash is important).
*/



public class Extensions  {

    /**
    * Convert a result tree fragment to a node-set.
    * Redundant: an RTF now *is* a node-set
    */

    public static NodeSetValue nodeset(Value frag) throws SAXException {
        if (frag instanceof NodeSetValue) {
            return (NodeSetValue)frag;
        } else {
            return new FragmentValue(frag.asString());
        }        
    }

    /**
    * Return the system identifier of the context node
    */

    public static String systemid(Context c) throws SAXException {
        return c.getContextNode().getSystemId();
    }

    /**
    * Return the line number of the context node.
    * This must be returned as a double to meet the calling requirements for extension functions.
    */

    public static double linenumber(Context c) throws SAXException {
        return c.getContextNode().getLineNumber();
    }

    /**
    * Return the intersection of two node-sets
    * @param p1 The first node-set
    * @param p2 The second node-set
    * @return A node-set containing all nodes that are in both p1 and p2
    */

    public static NodeSetValue intersection(NodeSetValue p1, NodeSetValue p2) throws SAXException {
        NodeEnumeration e1 = p1.enumerate();
        NodeEnumeration e2 = p2.enumerate();
        NodeEnumeration intersection = new IntersectionEnumeration(e1, e2);
        return new NodeSetExtent(intersection);
    }

    /**
    * Return the difference of two node-sets
    * @param p1 The first node-set
    * @param p2 The second node-set
    * @return A node-set containing all nodes that are in p1 and not in p2
    */

    public static NodeSetValue difference(NodeSetValue p1, NodeSetValue p2) throws SAXException {
        NodeEnumeration e1 = p1.enumerate();
        NodeEnumeration e2 = p2.enumerate();
        NodeEnumeration difference = new DifferenceEnumeration(e1, e2);
        return new NodeSetExtent(difference);
    }

    /**
    * Determine whether two node-sets contain the same nodes
    * @param p1 The first node-set
    * @param p2 The second node-set
    * @return true if p1 and p2 contain the same set of nodes
    */

    public static boolean hasSameNodes(NodeSetValue p1, NodeSetValue p2) throws SAXException {
        if (p1.getCount()!=p2.getCount()) return false;
        NodeEnumeration e1 = p1.enumerate();
        NodeEnumeration e2 = p2.enumerate();
        NodeEnumeration difference = new DifferenceEnumeration(e1, e2);
        return !(difference.hasMoreElements());
    }


    /**
    * Return the value of the second argument if the first is true, or the third argument
    * otherwise. Note that all three arguments are evaluated.
    * @param test A value treated as a boolean
    * @param thenValue Any value
    * @param elseValue Any value
    * @return (test ? thenValue : elseValue)
    */

    public static Value IF (Value test, Value thenValue, Value elseValue ) throws SAXException {
        return ( test.asBoolean() ? thenValue : elseValue );
    }

    /**
    * Evaluate the expression supplied in the first argument as a string 
    */

    public static Value evaluate (Context c, String expr) throws SAXException {
        Expression e = Expression.make(expr, c.getStaticContext());
        return e.evaluate(c);
    }

    /**
    * Evaluate the stored expression supplied in the first argument 
    */

    public static Value eval (Context c, Expression expr) throws SAXException {
        return expr.evaluate(c);
    }

    /**
    * Return an object representing a stored expression,
    * from the string supplied in the first argument. 
    */

    public static Value expression (Context c, String expr) throws SAXException {
        Expression e1 = Expression.make(expr, c.getStaticContext());
        // substitute values of variables
        Expression e2 = e1.reduce(Context.VARIABLES, c).simplify();
        return new ObjectValue(e2);
    }

    /**
    * Total a stored expression over a set of nodes
    */

    public static double sum (Context context,
                              NodeSetValue nsv,
                              Expression expression) throws SAXException {
        double total = 0.0;
        NodeInfo[] v = nsv.getNodes();
        Context c = context.newContext();
        c.setLast(v.length);
        for (int i=0; i<v.length; i++) {
            c.setContextNode(v[i]);
            c.setPosition(i+1);
            double x = expression.evaluateAsNumber(c);
            total += x;
        }
        return total;
    }

    /**
    * Get the maximum numeric value of the string-value of each of a set of nodes
    */

    public static double max (NodeSetValue nsv) throws SAXException {
        double max = Double.NEGATIVE_INFINITY;
        NodeInfo[] v = nsv.getNodes();
        for (int i=0; i<v.length; i++) {
            double x = Value.stringToNumber(v[i].getValue());
            if (x>max) max = x;
        }
        return max;
    }


    /**
    * Get the maximum numeric value of a stored expression over a set of nodes
    */

    public static double max (Context context,
                              NodeSetValue nsv,
                              Expression expression) throws SAXException {
        double max = Double.NEGATIVE_INFINITY;
        NodeInfo[] v = nsv.getNodes();
        Context c = context.newContext();
        c.setLast(v.length);
        for (int i=0; i<v.length; i++) {
            c.setContextNode(v[i]);
            c.setPosition(i+1);
            double x = expression.evaluateAsNumber(c);
            if (x>max) max = x;
        }
        return max;
    }

    /**
    * Get the minimum numeric value of the string-value of each of a set of nodes
    */

    public static double min (NodeSetValue nsv) throws SAXException {
        double min = Double.POSITIVE_INFINITY;
        NodeInfo[] v = nsv.getNodes();
        for (int i=0; i<v.length; i++) {
            double x = Value.stringToNumber(v[i].getValue());
            if (x<min) min = x;
        }
        return min;
    }

    /**
    * Get the minimum numeric value of a stored expression over a set of nodes
    */

    public static double min (Context context,
                              NodeSetValue nsv,
                              Expression expression) throws SAXException {
        double min = Double.POSITIVE_INFINITY;
        NodeInfo[] v = nsv.getNodes();
        Context c = context.newContext();
        c.setLast(v.length);
        for (int i=0; i<v.length; i++) {
            c.setContextNode(v[i]);
            c.setPosition(i+1);
            double x = expression.evaluateAsNumber(c);
            if (x<min) min = x;
        }
        return min;
    }

    /**
    * Get the node with maximum numeric value of the string-value of each of a set of nodes
    */

    public static NodeSetValue highest (NodeSetValue nsv) throws SAXException {
        double max = Double.NEGATIVE_INFINITY;
        NodeInfo highest = null;
        NodeInfo[] v = nsv.getNodes();
        for (int i=0; i<v.length; i++) {
            double x = Value.stringToNumber(v[i].getValue());
            if (x>max) {
                max = x;
                highest = v[i];
            }
        }
        return new SingletonNodeSet(highest);
    }


    /**
    * Get the maximum numeric value of a stored expression over a set of nodes
    */

    public static NodeSetValue highest (Context context,
                                        NodeSetValue nsv,
                                        Expression expression) throws SAXException {
        double max = Double.NEGATIVE_INFINITY;
        NodeInfo highest = null;
        NodeInfo[] v = nsv.getNodes();
        Context c = context.newContext();
        c.setLast(v.length);
        for (int i=0; i<v.length; i++) {
            c.setContextNode(v[i]);
            c.setPosition(i+1);
            double x = expression.evaluateAsNumber(c);
            if (x>max) {
                max = x;
                highest = v[i];
            }
        }
        return new SingletonNodeSet(highest);
    }

    /**
    * Get the node with minimum numeric value of the string-value of each of a set of nodes
    */

    public static NodeSetValue lowest (NodeSetValue nsv) throws SAXException {
        double min = Double.POSITIVE_INFINITY;
        NodeInfo lowest = null;
        NodeInfo[] v = nsv.getNodes();
        for (int i=0; i<v.length; i++) {
            double x = Value.stringToNumber(v[i].getValue());
            if (x<min) {
                min = x;
                lowest = v[i];
            }
        }
        return new SingletonNodeSet(lowest);
    }

    /**
    * Get the node with minimum numeric value of a stored expression over a set of nodes
    */

    public static NodeSetValue lowest (Context context,
                                       NodeSetValue nsv,
                                       Expression expression) throws SAXException {
        double min = Double.POSITIVE_INFINITY;
        NodeInfo lowest = null;
        NodeInfo[] v = nsv.getNodes();
        Context c = context.newContext();
        c.setLast(v.length);
        for (int i=0; i<v.length; i++) {
            c.setContextNode(v[i]);
            c.setPosition(i+1);
            double x = expression.evaluateAsNumber(c);
            if (x<min) {
                min = x;
                lowest = v[i];
            }
        }
        return new SingletonNodeSet(lowest);
    }



    /**
    * Given a node-set, return a subset that includes only nodes with distinct string-values
    */

    public static NodeSetValue distinct(NodeSetValue in) throws SAXException {
        Hashtable values = new Hashtable();
        Vector v = new Vector();
        NodeEnumeration enuma = in.enumerate();
        while (enuma.hasMoreElements()) {
            NodeInfo node = enuma.nextElement();
            String val = node.getValue();
            if (values.get(val)==null) {
                values.put(val, node);
                v.addElement(node);
            }
        }
        NodeSetValue out = new NodeSetExtent(v);
        out.setSorted(in.isSorted());
        return out;
    }

    /**
    * Given a node-set, return a subset that includes only nodes with distinct string-values
    * for the supplied expression
    */

    public static NodeSetValue distinct(Context context,
                                        NodeSetValue in,
                                        Expression exp) throws SAXException {
        Hashtable values = new Hashtable();
        Vector result = new Vector();
        NodeInfo[] nodes = in.getNodes();
        Context c = context.newContext();
        c.setLast(nodes.length);
        for (int i=0; i<nodes.length; i++) {
            c.setContextNode(nodes[i]);
            c.setPosition(i+1);
            String val = exp.evaluateAsString(c);
            if (values.get(val)==null) {
                values.put(val, nodes[i]);
                result.addElement(nodes[i]);
            }
        }
        NodeSetValue out = new NodeSetExtent(result);
        out.setSorted(in.isSorted());
        return out;
    }

    /**
    * Get the nodes that staisfy the given expression, up to and excluding the first one
    * (in document order) that doesn't
    */

    public static NodeSetValue leading (Context context,
                         NodeSetValue in, Expression exp) throws SAXException {
        Vector result = new Vector();
        in.sort();
        NodeInfo[] nodes = in.getNodes();
        Context c = context.newContext();
        c.setLast(nodes.length);
        for (int i=0; i<nodes.length; i++) {
            c.setContextNode(nodes[i]);
            c.setPosition(i+1);
            boolean val = exp.evaluateAsBoolean(c);
            if (val) {  
                result.addElement(nodes[i]);
            } else {
                break;
            }
        }
        NodeSetValue out = new NodeSetExtent(result);
        out.setSorted(true);
        return out;
        
    }

    /**
    * Test whether node-set contains a node that satisfies a given condition
    */

    public static boolean exists (Context context,
                              NodeSetValue nsv,
                              Expression expression) throws SAXException {
        NodeInfo[] v = nsv.getNodes();
        Context c = context.newContext();
        c.setLast(v.length);
        for (int i=0; i<v.length; i++) {
            c.setContextNode(v[i]);
            c.setPosition(i+1);
            boolean x = expression.evaluateAsBoolean(c);
            if (x) return true;
        }
        return false;
    }

    /**
    * Test whether all nodes in a node-set satisfy a given condition
    */

    public static boolean forAll (Context context,
                              NodeSetValue nsv,
                              Expression expression) throws SAXException {
        NodeInfo[] v = nsv.getNodes();
        Context c = context.newContext();
        c.setLast(v.length);
        for (int i=0; i<v.length; i++) {
            c.setContextNode(v[i]);
            c.setPosition(i+1);
            boolean x = expression.evaluateAsBoolean(c);
            if (!x) return false;
        }
        return true;
    }


    /**
    * Return a node-set whose nodes have string-values "1", "2", ... "n"
    */

    public static NodeSetValue range(Context context, double start, double finish) throws SAXException {
        int a = (int)Round.round(start);
        int b = (int)Round.round(finish);
        
        FragmentValue frag = new FragmentValue();
        Controller c = context.getController();
        OutputDetails details = new OutputDetails();
        details.setMethod("saxon:fragment");
        details.setEmitter(frag.getEmitter());
        c.setNewOutputDetails(details);
        Outputter out = c.getOutputter();

        for (int i=a; i<=b; i++) {
            out.writeStartTag(rangeName);
            out.writeContent(i+"");
            out.writeEndTag(rangeName);
        }
                    
        c.resetOutputDetails();
        return new NodeSetExtent(frag.getFirst().getAllChildNodes());        
    }

    private static Name rangeName = new Name("saxon-range");

    /**
    * Return a node-set by tokenizing a supplied string. Tokens are delimited by any sequence of
    * whitespace characters.
    */

    public static NodeSetValue tokenize(Context context, String s) throws SAXException {
        
        FragmentValue frag = new FragmentValue();
        Controller c = context.getController();
        OutputDetails details = new OutputDetails();
        details.setMethod("saxon:fragment");
        details.setEmitter(frag.getEmitter());
        c.setNewOutputDetails(details);
        Outputter out = c.getOutputter();

        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            out.writeStartTag(tokenName);
            out.writeContent(st.nextToken());
            out.writeEndTag(tokenName);
        }
                    
        c.resetOutputDetails();
        return new NodeSetExtent(frag.getFirst().getAllChildNodes());       
    }

    /**
    * Return a node-set by tokenizing a supplied string. The argument delim is a String, any character
    * in this string is considered to be a delimiter character, and any sequence of delimiter characters
    * acts as a separator between tokens. 
    */

    public static NodeSetValue tokenize(Context context, String s, String delim) throws SAXException {
        
        FragmentValue frag = new FragmentValue();
        Controller c = context.getController();
        OutputDetails details = new OutputDetails();
        details.setMethod("saxon:fragment");
        details.setEmitter(frag.getEmitter());
        c.setNewOutputDetails(details);
        Outputter out = c.getOutputter();

        StringTokenizer st = new StringTokenizer(s, delim);
        while (st.hasMoreTokens()) {
            out.writeStartTag(tokenName);
            out.writeContent(st.nextToken());
            out.writeEndTag(tokenName);
        }
                    
        c.resetOutputDetails();
        return new NodeSetExtent(frag.getFirst().getAllChildNodes());       
    }


    private static Name tokenName = new Name("saxon-token");


    /**
    * Return an XPath expression that identifies the current node
    */

    public static String path(Context c) throws SAXException {
        return c.getContextNode().getPath();
    }

    /**
    * Test whether an encapsulated Java object is null
    */

    public static boolean isNull(Object x) throws SAXException {
        return x==null;
    }

    /**
    * Save a value associated with the context node 
    */

    public static void setUserData(Context c, String name, Value value) throws SAXException {
        c.getController().setUserData(c.getContextNode(), name, value);
    }

    /**
    * Retrieve a value associated with the context node
    */

    public static Value getUserData(Context c, String name) throws SAXException {
        Object o = c.getController().getUserData(c.getContextNode(), name);
        if (o==null) return new StringValue("");
        if (o instanceof Value) return (Value)o;
        return new ObjectValue(o);
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
