package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.style.SAXONFunction;
import com.icl.saxon.om.NodeInfo;

import org.xml.sax.SAXException;
import java.util.*;


/**
* This class represents a call to a function defined in the stylesheet
*/

public class StyleSheetFunctionCall extends Function {

    private SAXONFunction function;
    private Controller boundController = null;
    private NodeInfo boundContextNode = null;
    private int boundContextPosition = -1;
    private int boundContextSize = -1;

    /**
    * Create the reference to the saxon:function element
    */

    public void setFunction(SAXONFunction f) {
        function = f;
    }

    public Function newInstance() {
        StyleSheetFunctionCall nf = new StyleSheetFunctionCall();
        nf.setFunction(function);
        nf.setStaticContext(getStaticContext());
        return nf;
    }

    /**
    * Get the name of the function.
    * This method must be implemented in all subclasses.
    * @return the name of the function, as used in XSL expressions, but excluding
    * its namespace prefix
    */

    public String getName() {
        return function.getAttribute("name");   // not quite as specified
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getIntrinsicDependencies() {
        
        // we could do better than this by examining the XSLT code
        
        int dep = Context.NO_DEPENDENCIES;
        if (boundController==null) dep |= Context.CONTROLLER;
        if (boundContextNode==null) dep |= Context.CONTEXT_NODE;
        if (boundContextPosition==-1) dep |= Context.POSITION;
        if (boundContextSize==-1) dep |= Context.LAST;
        return dep;

    }

    /**
    * Remove intrinsic dependencies. An "intrinsic" dependency
    * is one in the function itself, as distinct from a dependency in its arguments.
    */

    protected Expression reduceIntrinsic(int dependencies, Context context)
            throws SAXException {
        
        if ((dependencies & Context.CONTROLLER) != 0) {
            boundController = context.getController();
        }
        if ((dependencies & Context.CONTEXT_NODE) != 0) {
            boundContextNode = context.getContextNode();
        } 
        if ((dependencies & Context.POSITION) != 0) {
            boundContextPosition = context.getPosition();
        }
        if ((dependencies & Context.LAST) != 0) {
            boundContextSize = context.getLast();
        }

        return this;
    }

    /**
    * Evaluate the function, having evaluated its arguments. <br>
    * This method must be implemented in all subclasses.
    * @param arguments A Vector, each of whose elements is a Value containing the value of a
    * supplied argument to the function.
    * @param context The context in which the function is to be evaluated
    * @return a Value representing the result of the function. This must be of the data type
    * corresponding to the result of getType().
    * @throws SAXException if the function cannot be evaluated.
    */    

    public Value eval(Vector arguments, Context context) throws SAXException {
        if (boundController!=null) {
            context.setController(boundController);
            context.setBindery(boundController.getBindery());
        }
        if (boundContextNode!=null) {
            context.setCurrentNode(boundContextNode);
            context.setContextNode(boundContextNode);
        }
        if (boundContextPosition!=-1) {
            context.setPosition(boundContextPosition);
        }
        if (boundContextSize!=-1) {
            context.setLast(boundContextSize);
        }

        ParameterSet ps = new ParameterSet();
        for (int i=0; i<arguments.size(); i++) {
            String param = function.getNthParameter(i);
            if (param==null) {
                throw new SAXException("Too many arguments");
            }
            ps.put(param, (Value)arguments.elementAt(i));
        }
        return function.call(ps, context);
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
