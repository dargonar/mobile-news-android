package com.icl.saxon.expr;
import com.icl.saxon.*;

import org.xml.sax.SAXException;
import java.util.*;


/**
* Abstract superclass for system-defined and user-defined functions
*/

public abstract class Function extends Expression {

    protected Vector arguments = new Vector(4);

    /**
    * Method to add an argument during function definition.
    */

    public void addArgument(Expression expr) {
        arguments.addElement(expr);
    }

    /**
    * Get the name of the function.
    * This method must be implemented in all subclasses.
    * @return the name of the function, as used in XSL expressions, but excluding
    * its namespace prefix
    */

    public abstract String getName();

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        return basicSimplify();
    }

    /**
    * BasicSimplify does a simplify() on each of the arguments
    */

    private Expression basicSimplify() throws SAXException {
        for (int i=0; i<arguments.size(); i++) {
            Expression arg = ((Expression)arguments.elementAt(i)).simplify();
            arguments.setElementAt(arg, i);
        }
        return this;
    }

    /**
    * Method to simplify a pure function, ie. one which is not context dependent.
    * This method may be invoked from the simplify() method of a subclass. If all
    * the arguments of the function are Values, it pre-evaluates the function and returns
    * the result.
    */

    public Expression simplifyPureFunction() throws SAXException {
        basicSimplify();
        for (int i=0; i<arguments.size(); i++) {
            Expression arg = ((Expression)arguments.elementAt(i));
            if (!(arg instanceof Value)) return this;   // can't pre-evaluate
        }
        return this.evaluate(null);     // the context will not be used
    }

    /**
    * Evaluate the function: evaluate the arguments, then call the function's eval() method
    */

    public Value evaluate(Context c) throws SAXException {    
        if (arguments.size()==0) {              // fast path
            return eval(arguments, c);
        }
        
        Vector argvalues = new Vector(arguments.size());
        for (int i=0; i<arguments.size(); i++) {
            Value v = ((Expression)arguments.elementAt(i)).evaluate(c);
            argvalues.addElement(v);
        }
        return eval(argvalues, c);
    }    

    /**
    * Get the intrinsic dependencies for the function, independent of the arguments
    */

    public int getIntrinsicDependencies() {
        return Context.NO_DEPENDENCIES;       // default value unless overriden
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = getIntrinsicDependencies();
        for (int i=0; i<arguments.size(); i++) {
            Expression e = (Expression)arguments.elementAt(i);
            dep |= e.getDependencies();
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
            Function newFunction = newInstance();
            
            if (arguments.size() > 0) {
                for (int i=0; i<arguments.size(); i++) {
                    Expression arg = (Expression)arguments.elementAt(i);
                    Expression newarg = arg.reduce(dependencies, context);                   
                    newFunction.addArgument(newarg);
                }
            }

            if ((dependencies & getIntrinsicDependencies()) != 0) {
                Expression fin = newFunction.reduceIntrinsic(dependencies, context);
                fin.setStaticContext(getStaticContext());
                return fin.simplify();
            } else {
                newFunction.setStaticContext(getStaticContext());
                return newFunction.simplify();
            }
        } else {
            return this;
        }
    }

    /**
    * Make a new instance of this function
    */

    public Function newInstance() throws SAXException {
        Function newFunction;
        try {
            newFunction = (Function)this.getClass().newInstance();
            newFunction.setStaticContext(getStaticContext());
        } catch (Exception err) {
            throw new SAXException("Internal failure replicating function instance", err);
        }
        return newFunction;
    }

    /**
    * Remove intrinsic dependencies. Default implementation. An "intrinsic" dependency
    * is one in the function itself, as distinct from a dependency in its arguments.
    */

    protected Expression reduceIntrinsic(int dependencies, Context context)
            throws SAXException {
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

    public abstract Value eval(Vector arguments, Context context) throws SAXException;

    /**
    * Get a string representation of the function call
    */

    public String toString() {
        String s = getName() + "(";
        for (int i=0; i<arguments.size(); i++) {
            s += arguments.elementAt(i).toString();
            if (i<arguments.size()-1) s+= ", ";
        }
        return s + ")";
    }

    /**
    * Check number of arguments. <BR>
    * A convenience routine for use in subclasses.
    * @param min the minimum number of arguments allowed
    * @param max the maximum number of arguments allowed
    * @return the actual number of arguments
    * @throws SAXException if the number of arguments is out of range
    */

    protected int checkArgumentCount(int min, int max) throws SAXException {
        int numArgs = arguments.size();
        if (min==max && numArgs != min) {
            throw new SAXException("Function " + getName() + " must have " + min + pluralArguments(min));
        }
        if (numArgs < min) {
            throw new SAXException("Function " + getName() + " must have at least " + min + pluralArguments(min));
        }
        if (numArgs > max) {
            throw new SAXException("Function " + getName() + " must have no more than " + max + pluralArguments(max));
        }
        return numArgs;
    }

    /**
    * Utility routine used in constructing error messages
    */

    private String pluralArguments(int num) {
        if (num==1) return " argument";
        return " arguments";
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
