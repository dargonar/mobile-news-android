package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.functions.*;
import org.xml.sax.SAXException;
import java.util.*;



/**
* This class serves two purposes: it is an abstract superclass for different kinds of XPath expression,
* and it contains a static method to invoke the expression parser
*/

public abstract class Expression  {

    protected StaticContext staticContext;
    protected static Vector emptyVector = new Vector();

    /**
    * Parse an expression
    * @param expression The expression (as a character string)
    * @param env An object giving information about the compile-time context of the expression
    * @return an object of type Expression
    */

    public static Expression make(String expression, StaticContext env) throws SAXException {
        try {
            Expression exp = (new ExpressionParser()).parse(expression, env).simplify();  
            exp.staticContext = env;
            return exp;
        } catch (SAXException err) {
            if (env.forwardsCompatibleModeIsEnabled()) {
                return new ErrorExpression(err);
            } else {
                throw err;
            }
        }
    }

    /**
    * Parse an expression using a default compile-time context. This default context does not support
    * the use of context-dependent facilities in expressions, including namespaces, use of
    * variables, or use of the document() function (which needs to know a base URI)
    * @param expression The expression (as a character string)
    * @param env An object giving information about the compile-time context of the expression
    * @return an object of type Expression
    */

    public static Expression make(String expression) throws SAXException {
        return make(expression, new DummyStaticContext());
    }

    /**
    * Simplify an expression. Default implementation does nothing.
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        return this;
    };

    /**
    * Set the static context used when the expression was parsed
    */

    public final void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }

    /**
    * Determine the static context used when the expression was parsed
    */

    public final StaticContext getStaticContext() {
        return staticContext;
    }

    /**
    * Determine whether the expression contains any references to variables 
    * @return true if so
    */

    public boolean containsReferences() throws SAXException {
        return (getDependencies() & Context.VARIABLES) != 0;
    }

    /**
    * Evaluate an expression. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public abstract Value evaluate(Context context) throws SAXException;

    /**
    * Evaluate an expression as a Boolean.<br>
    * The result of x.evaluateAsBoolean(c) must be equivalent to x.evaluate(c).asBoolean();
    * but optimisations are possible when it is known that a boolean result is required,
    * especially in the case of a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public boolean evaluateAsBoolean(Context context) throws SAXException {
        return evaluate(context).asBoolean();
    }

    /**
    * Evaluate an expression as a Number.<br>
    * The result of x.evaluateAsNumber(c) must be equivalent to x.evaluate(c).asNumber();
    * but optimisations are possible when it is known that a numeric result is required,
    * especially in the case of a NodeSet. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public double evaluateAsNumber(Context context) throws SAXException {
        return evaluate(context).asNumber();
    }        

    /**
    * Evaluate an expression as a String.<br>
    * The result of x.evaluateAsString(c) must be equivalent to x.evaluate(c).asString();
    * but optimisations are possible when it is known that a string result is required,
    * especially in the case of a NodeSet. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public String evaluateAsString(Context context) throws SAXException {
        return evaluate(context).asString();
    }

    /**
    * Evaluate an expression as a NodeSet.<br>
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    * @throws SAXException when the expression is not a nodeset expression.
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws SAXException {
        // Default implementation: see also NodeSetExpression
        Value val = evaluate(context);
        if (val instanceof NodeSetValue)
            return ((NodeSetValue)val);        
        throw new SAXException("Cannot convert value [" + val.toString() + "] to a node-set");
    }

    /**
    * Return an enumeration of nodes in a nodeset. 
    * @param context The context in which the expression is to be evaluated
    * @throws SAXException when the expression is not a nodeset expression.
    */

    public NodeEnumeration enumerate(Context context) throws SAXException {
        // default implementation: see also NodeSetExpression
        Value val = evaluate(context);
        if (val instanceof NodeSetValue) {
            return ((NodeSetValue)val).enumerate();
        }
        throw new SAXException("Cannot convert value [" + val.toString() + "] to a node-set");
    }

    /**
    * Determine the data type of the expression, if possible
    * @return one of the values Value.STRING, Value.BOOLEAN, Value.NUMBER, Value.NODESET,
    * Value.FRAGMENT, or Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        return Value.ANY;
    }

    /**
    * Determine whether the return the type of the expression is numeric. <br>
    * This information is needed if the expression is used as a filter in a path expression
    * or pattern. If false, the value MAY be numeric (e.g. a variable reference)
    * @return true if the expression will definitely return a numeric value. 
    */

    public boolean isNumeric() {
        return getDataType()==Value.NUMBER;
    }

    /**
    * Determine whether the value of the expression is dependent on the position of the current
    * node in the current node list or on the size of the current node list. <br>
    * This information is useful when the expression is used as a filter in a pattern:
    * if the expression is not relative, there is no need to evaluate the current node list.
    * @return true if the value of the expression depends on position() or last() or if it uses
    * a numeric predicate. This default implementation returns true only if one of the subexpressions
    * is relative.
    */

    public final boolean isRelative() throws SAXException {
        return (getDependencies() & (Context.POSITION | Context.LAST)) != 0;
    }

    /**
    * Determine whether the expression uses the current() function. This is an error if the
    * expression is within a pattern
    */

    public boolean usesCurrent() throws SAXException {
        return (getDependencies() & Context.CURRENT_NODE) != 0;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public abstract int getDependencies();

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed, e.g. Context.VARIABLES
    * @param context The context to be used for the partial evaluation
    * @return a new expression (or Value) that does not have any of the specified dependencies
    */

    public abstract Expression reduce(int dependencies, Context context) throws SAXException;

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
// The line marked PB-SYNC is by Peter Bryant (pbryant@bigfoot.com). All Rights Reserved. 
//
// Contributor(s): Michael Kay, Peter Bryant
//
