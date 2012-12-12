package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.axis.*;

import org.xml.sax.SAXException;
import java.util.*;

/**
* An expression that establishes a set of nodes by following relationships between nodes
* in the document. Specifically, it consists of a start expression which defines a set of
* nodes, and a Step which defines a relationship to be followed from those nodes to create
* a new set of nodes.
*/

public class PathExpression extends NodeSetExpression {

    private Expression start;
    private Step step;

    /**
    * Constructor
    * @param start A node-set expression denoting the absolute or relative set of nodes from which the
    * navigation path should start.
    * @param step The step to be followed from each node in the start expression to yield a new
    * node-set
    */

    public PathExpression(Expression start, Step step) {
        this.start = start;
        this.step = step;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        
        start = start.simplify();
        step = step.simplify();

        // if the start expression is an empty node-set, then the whole PathExpression is empty
        if (start instanceof EmptyNodeSet) {
            return start;
        }

        // if the simplified Step is null, then by convention the whole PathExpression is empty
        if (step==null) {
            return new EmptyNodeSet();
        }
        
        int axisNumber = step.getAxis().getAxisNumber();
        
        // simplify a straightforward attribute reference such as "@name"
        if ( start instanceof ContextNodeExpression &&
                axisNumber == Axis.ATTRIBUTE &&
                step.getNodeName() instanceof Name &&
                step.getFilters().size() == 0) {
            return new AttributeReference((Name)step.getNodeName());
        }

        // Simplify an expression of the form a//b, where b has no filters.
        // This comes out of the parser as a/descendent-or-self::node()/child::b,
        // but it is equivalent to a/descendant::b; and the latter is better as it
        // doesn't require sorting

        // This optimisation isn't working, because getNodeName() is an AnyNameTest, not null.
        // Fixed in a future version; but in the present code I've commented it out to avoid
        // the risk of new bugs.
        
        //if ( axisNumber == Axis.CHILD &&
        //        step.getFilters().size() == 0 &&
        //        start instanceof PathExpression &&
        //        ((PathExpression)start).step.getAxis().getAxisNumber() == Axis.DESCENDANT_OR_SELF &&
        //        ((PathExpression)start).step.getFilters().size() == 0 &&
        //        ((PathExpression)start).step.getNodeName() == null &&
        //        ((PathExpression)start).step.getNodeType() == NodeInfo.NODE )
        //{
        //    return new PathExpression(
        //        ((PathExpression)start).start,
        //        new Step(Axis.DESCENDANT, step.getNodeType(), step.getNodeName()));
        //}

        return this;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = start.getDependencies();
        Vector filters = step.getFilters();
        for (int f=0; f<filters.size(); f++) {
            Expression exp = (Expression)filters.elementAt(f);
            // Not all dependencies in the filter matter, because the context node, etc,
            // are not dependent on the outer context of the PathExpression
            dep |= (exp.getDependencies() & Context.XSLT_CONTEXT);
        }
        return dep;
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
            Step newstep = new Step(step.getAxis(), step.getNodeType(), step.getNodeName());
            Vector filters = step.getFilters();
            for (int f=0; f<filters.size(); f++) {
                Expression exp = (Expression)filters.elementAt(f);
                // Not all dependencies in the filter matter, because the context node, etc,
                // are not dependent on the outer context of the PathExpression
                Expression newfilter = exp.reduce(dependencies & Context.XSLT_CONTEXT, context);
                newstep.addFilter(newfilter);               
            }
            Expression e = new PathExpression(newstart, newstep);
            e.setStaticContext(getStaticContext());
            return e.simplify();
        } else {
            return this;
        }
    }


    /**
    * Evaluate the path-expression in a given context to return a NodeSet
    * @param context the evaluation context
    */

    public NodeEnumeration enumerate(Context context) throws SAXException {   
        return new PathEnumeration(start, context);
    }


    /**
    * Return a string representation of the expression
    * Used for diagnostics, and also for reconstituting the expression in the compiler
    */

    public String toString() {
        String s = start.toString();
        if (s.equals("/")) s = "";      // otherwise we get an extra "/" at the start
        return s + "/" + step.toString();
    }

    /**
    * Inner class PathEnumeration
    */

    private class PathEnumeration implements NodeEnumeration {

        private Expression thisStart;
        private NodeEnumeration base=null;
        private NodeEnumeration thisStep=null;
        private NodeInfo next=null;
        private Context context;

        public PathEnumeration(Expression start, Context context) throws SAXException {
            thisStart = start;
            if (context==null) {
                this.context = new Context();
            } else {
                this.context = context.newContext();
            }
            base = start.enumerate(this.context);
            next = getNextNode();
        }

        public boolean hasMoreElements() throws SAXException {
            return next!=null;
        }

        public NodeInfo nextElement() throws SAXException {
            NodeInfo curr = next;
            next = getNextNode();
            return curr;
        }

        private NodeInfo getNextNode() throws SAXException {
            
            // if we are currently processing a step, we continue with it. Otherwise,
            // we get the next base element, and apply the step to that.
            
            if (thisStep!=null && thisStep.hasMoreElements()) {
                return thisStep.nextElement();
                                //NodeInfo n = thisStep.nextElement();
                                //System.err.println("Continuing Step.nextElement() = " + n);
                                //return n;
            }
            
            while (base.hasMoreElements()) {
                NodeInfo node = base.nextElement();
                                //System.err.println("Base.nextElement = " + node);
                thisStep = step.enumerate(node, context);
                if (thisStep.hasMoreElements()) {
                    return thisStep.nextElement();
                                //NodeInfo n2 = thisStep.nextElement();
                                //System.err.println("Starting Step.nextElement() = " + n2);
                                //return n2;
                }
            }
            
            return null;

        }

        /**
        * Determine if we can guarantee that the nodes are in document order. This is true if the
        * start nodes are sorted peer nodes and the step is within the subtree rooted at each node.
        * It is also true if the start is a singleton node and the axis is sorted.
        */

        public boolean isSorted() throws SAXException {
            return (base.isSorted() && base.isPeer() && step.getAxis().isWithinSubtree()) ||
                   (thisStart instanceof SingletonExpression && step.getAxis().isSorted());
        }

	    /**
	    * Determine if the nodes are guaranteed to be in reverse document order. This is true if the
	    * base is singular (e.g. the root node or the current node) and the axis is a reverse axis
	    */

        public boolean isReverseSorted() throws SAXException {
            return thisStart instanceof SingletonExpression && step.getAxis().isReverseSorted();
        }

        /**
        * Determine if the resulting nodes are peer nodes, that is, if no node is a descendant of any
        * other. This is the case if the start nodes are peer nodes and the axis is a peer axis.
        */

        public boolean isPeer() throws SAXException {
            return (base.isPeer() && step.getAxis().isPeer());
        }

    }   // end of inner class PathEnumeration


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
