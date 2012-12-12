package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.output.*;
import com.icl.saxon.style.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.net.URL;
import java.io.Writer;


/**
* This class represents a context in which an expression is evaluated or a template is executed
* (as defined in the XSLT specification). It also provides a range of services to node handlers,
* for example access to the outputter and bindery, and the applyTemplates() function.
*/

public class Context implements LastPositionFinder {

    // Define the different kinds of context-dependency in an expression

    public static final int VARIABLES = 1;      // Expression depends on values of variables
    public static final int CURRENT_NODE = 4;   // Expression depends on current() node
    public static final int CONTEXT_NODE = 8;   // Expression depends on context node
    public static final int POSITION = 16;      // Expression depends on position()
    public static final int LAST = 32;          // Expression depends on last()
    public static final int CONTROLLER = 64;    // Expression evaluation needs the Controller
    public static final int NO_DEPENDENCIES = 0;
    public static final int ALL_DEPENDENCIES = 255;
    public static final int XSLT_CONTEXT = CONTROLLER | VARIABLES | CURRENT_NODE;

    private NodeInfo contextNode;
    private NodeInfo currentNode;
    private int position;
    private int last;
    private LastPositionFinder lastPositionFinder;
    private Controller controller;
    private Bindery bindery;
    private Mode currentMode;
    private XSLTemplate currentTemplate;
    private Stack groupActivationStack;     // holds stack of active saxon:group activations
    private StaticContext staticContext;
    private ParameterSet tailRecursion;     // set when a tail-recursive call is requested
    private NodeInfo lastRememberedNode = null;
    private int lastRememberedNumber = -1;
    private Value returnValue = null;

    /**
    * Construct a Context for local XPath use (no links to Controller, bindery, etc)
    */

    public Context() {
        last = -1;
        position = -1;
        lastPositionFinder = this;
    };

    /**
    * Constructor should only be called by the Controller, which acts as a Context factory.
    */

    public Context(Controller c) {
        controller = c;
        bindery = c.getBindery();
        last = -1;
        lastPositionFinder = this;
        position = -1;
        contextNode = null;
        currentNode = null;
        currentTemplate = null;
        currentMode = null;
        groupActivationStack = null;
        tailRecursion = null;
        returnValue = null;
    }

    /**
    * Construct a new context as a copy of another
    */

    public Context newContext() {
        Context c = new Context();
        c.controller = controller;
        c.currentNode = currentNode;
        c.contextNode = contextNode;
        c.position = position;
        c.last = last;
        c.lastPositionFinder = lastPositionFinder;
        c.currentMode = currentMode;
        c.currentTemplate = currentTemplate;
        c.bindery = bindery;
        c.groupActivationStack = groupActivationStack;
        c.lastRememberedNode = lastRememberedNode;
        c.lastRememberedNumber = lastRememberedNumber;
        c.returnValue = null;
        return c;
    }

    /**
    * Set the controller for this Context
    */

    public void setController(Controller c) {
        controller = c;
    }

    /**
    * Get the controller for this Context
    */

    public Controller getController() {
        return controller;
    }

    /**
    * Get the Bindery used by this Context
    */

    public Bindery getBindery() {
        return bindery;
    }

    /**
    * Set the Bindery used by this Context
    */

    public void setBindery(Bindery b) {
        bindery = b;
    }


    /**
    * Get the current Outputter. This gives access to the writeStartTag, writeAttribute,
    * and writeEndTag methods
    * @return the current Outputter
    */

    public Outputter getOutputter() {
        return controller.getOutputter();
    }
    
    /**
    * Set the mode (for use by the built-in handlers)
    */

    public void setMode(Mode mode) {
        currentMode = mode;
    }

    /**
    * Get the current mode (for use by the built-in handlers)
    */

    public Mode getMode() {
        return currentMode;
    }

    /**
    * Set the context node. <br>
    * Note that this has no effect on position() or last(), which must be set separately.
    * @param node the node that is to be the context node. 
    */

    public void setContextNode(NodeInfo node) {
        this.contextNode = node;
    }

    /**
    * Get the context node
    * @return the context node
    */

    public NodeInfo getContextNode() {
        return contextNode;
    }

    /**
    * Set the context position
    */

    public void setPosition(int pos) {
        position = pos;
    }

    /**
    * Get the context position (the position of the context node in the context node list)
    * @return the context position (starting at one)
    */

    public int getPosition() {
        return position;
    }

    /**
    * Set the context size; this also makes the Context object responisble for returning the last()
    * position.
    */

    public void setLast(int last) {
        this.last = last;
        lastPositionFinder = this;
    }

    /**
    * Set the LastPositionFinder, another object that will do the work of returning the last()
    * position
    */

    public void setLastPositionFinder(LastPositionFinder finder) {
        lastPositionFinder = finder;
    }

    /**
    * Get the context size (the position of the last item in the current node list)
    * @return the context size
    */

    public int getLast() throws SAXException {
        return lastPositionFinder.getLastPosition();
    }

    /**
    * Get the last position, to be used only
    * when the context object is being used as the last position finder
    */

    public int getLastPosition() {
        return last;
    }

    /**
    * Set the current node. This is the node in the source document currently being processed
    * (e.g. by apply-templates).
    */

    public void setCurrentNode(NodeInfo node) {
        currentNode = node;
    }

    /**
    * Get the current node. This is the node in the source document currently being processed
    * (e.g. by apply-templates). It is not necessarily the same as the context node: the context
    * node can change in a sub-expression, the current node cannot.
    */

    public NodeInfo getCurrentNode() {
        return currentNode;
    }

    /**
    * Set the current template. This is used to support xsl:apply-imports
    */

    public void setCurrentTemplate(XSLTemplate template) {
        currentTemplate = template;
    }

    /**
    * Set the static context
    */

    public void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }

    /**
    * Get the static context. This is currently available only while processing an
    * extension function
    */

    public StaticContext getStaticContext() {
        return staticContext;
    }

    /**
    * Get the saxon:group activation stack
    */

    public Stack getGroupActivationStack() {
        if (groupActivationStack==null) {
            groupActivationStack = new Stack();
        }
        return groupActivationStack;
    }

    /**
    * Set the last remembered node, for node numbering purposes
    */

    public void setRememberedNumber(NodeInfo node, int number) {
        lastRememberedNode = node;
        lastRememberedNumber = number;
    }

    /**
    * Get the number of a node if it is the last remembered one.
    * @return the number of this node if known, else -1.
    */

    public int getRememberedNumber(NodeInfo node) {
        if (lastRememberedNode == node) return lastRememberedNumber;
        return -1;
    }

    /**
    * Apply imports. This supports xsl:apply-imports
    */

    public void applyImports() throws SAXException {
        if (currentTemplate==null) {
            throw new SAXException("There is no current template");
        }

        int min = currentTemplate.getMinImportPrecedence();
        int max = currentTemplate.getPrecedence()-1;
        controller.applyImports(this, currentMode, min, max);
    }

    /**
    * Set tail recursion parameters
    */

    public void setTailRecursion(ParameterSet p) {
        tailRecursion = p;
    }

    /**
    * Get tail recursion parameters
    */   

    public ParameterSet getTailRecursion() {
        return tailRecursion;
    }

    /**
    * Set return value from function
    */

    public void setReturnValue(Value value) {
        returnValue = value;
    }

    /**
    * Get the return value from function
    */

    public Value getReturnValue() {
        return returnValue;
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
