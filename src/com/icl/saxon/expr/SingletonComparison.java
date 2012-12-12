package com.icl.saxon.expr;
import com.icl.saxon.*;
import org.xml.sax.SAXException;
import java.util.Vector;

/**
* Singleton Comparison: A Relational Expression that compares a singleton node-set with a string
* or numeric value for equals, not-equals, greater-than or less-than.
*/

public class SingletonComparison extends Expression {

    SingletonExpression node;
    int operator;
    Value value;

    public SingletonComparison(SingletonExpression p1, int op, Value p2) {
        node = p1;
        operator = op;
        value = p2;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        return this;
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a BooleanValue representing the result of the comparison of the two operands
    */

    public Value evaluate(Context c) throws SAXException {
        return new BooleanValue(evaluateAsBoolean(c));
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a boolean representing the result of the numeric comparison of the two operands
    */

    public boolean evaluateAsBoolean(Context c) throws SAXException {
        boolean exists = node.evaluateAsBoolean(c);
        if (exists) {
            if (value instanceof StringValue) {
                switch (operator) {
                    case Tokenizer.EQUALS:
                        return node.evaluateAsString(c).equals(value.asString());
                    case Tokenizer.NE:
                        return !node.evaluateAsString(c).equals(value.asString());
                    case Tokenizer.LT:
                        return node.evaluateAsNumber(c) < value.asNumber();
                    case Tokenizer.LE:
                        return node.evaluateAsNumber(c) <= value.asNumber();
                    case Tokenizer.GT:
                        return node.evaluateAsNumber(c) > value.asNumber();
                    case Tokenizer.GE:
                        return node.evaluateAsNumber(c) >= value.asNumber();
                    default:
                        throw new SAXException("Bad operator in singleton comparison");
                }
            } else if (value instanceof NumericValue) {
                switch(operator) {
                    case Tokenizer.EQUALS:
                        return node.evaluateAsNumber(c) == value.asNumber();
                    case Tokenizer.NE:
                        return node.evaluateAsNumber(c) != value.asNumber();
                    case Tokenizer.LT:
                        return node.evaluateAsNumber(c) < value.asNumber();
                    case Tokenizer.LE:
                        return node.evaluateAsNumber(c) <= value.asNumber();
                    case Tokenizer.GT:
                        return node.evaluateAsNumber(c) > value.asNumber();
                    case Tokenizer.GE:
                        return node.evaluateAsNumber(c) >= value.asNumber();
                    default:
                        throw new SAXException("Bad operator in singleton comparison");
                }
            } else {
                throw new SAXException("Unrecognized type in singleton comparison");
            }

        } else {
            return false;
        }
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return node.getDependencies();    
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
        if ((node.getDependencies() & dependencies) != 0 ) {
            Expression e = node.reduce(dependencies, context);
            if (e instanceof SingletonExpression) {
                e = new SingletonComparison(
                                (SingletonExpression)e,
                                operator,
                                value);
                e.setStaticContext(getStaticContext());
                return e.simplify();
            } else if (e instanceof NodeSetValue) {
                return new BooleanValue(((NodeSetValue)e).compare(operator, value));
            } else {
                throw new SAXException("Failed to reduce SingletonComparison: returned " + e.getClass());
            }
        } else {
            return this;
        }
    }

    /**
    * Display the expression as a string
    */

    public String toString() {
        return "(" + node.toString() +
                 " " + Tokenizer.tokens[operator] + " " +
                 value.toString() + ")";
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
