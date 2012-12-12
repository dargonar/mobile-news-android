package com.icl.saxon.expr;
import com.icl.saxon.*;
import org.xml.sax.SAXException;

/**
* Arithmetic Expression: a numeric expression consisting of the sum, difference,
* product, quotient, or modulus of two numeric expressions
*/

class ArithmeticExpression extends BinaryExpression {

    public ArithmeticExpression(){};

    public ArithmeticExpression(Expression p1, int operator, Expression p2) {
        super(p1, operator, p2);
    }

    /**
    * Evaluate an expression. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression as a Value object, evaluated in the current context
    */

    public Value evaluate(Context c) throws SAXException {
        return new NumericValue(evaluateAsNumber(c));
    }

    /**
    * Evaluate an expression as a Number. 
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression as a double, evaluated in the current context
    */

    public double evaluateAsNumber(Context c) throws SAXException {
        switch(operator) {
            case Tokenizer.PLUS:                
                return p1.evaluateAsNumber(c) + p2.evaluateAsNumber(c);
            case Tokenizer.MINUS:                
                return p1.evaluateAsNumber(c) - p2.evaluateAsNumber(c);
            case Tokenizer.MULT:                
                return p1.evaluateAsNumber(c) * p2.evaluateAsNumber(c);
            case Tokenizer.DIV:                
                return p1.evaluateAsNumber(c) / p2.evaluateAsNumber(c);
            case Tokenizer.MOD:                
                return p1.evaluateAsNumber(c) % p2.evaluateAsNumber(c);
            case Tokenizer.NEGATE:
                return -p2.evaluateAsNumber(c);

            default:
                throw new SAXException("Unknown operator in arithmetic expression");
        }                
    }

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.NUMBER
    */

    public int getDataType() {
        return Value.NUMBER;
    }
    
    /**
    * Decide if the expression is numeric 
    * @return true (always)
    */

    public boolean isNumeric() {
        return true;
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
        if ((getDependencies() & dependencies) != 0 ) {
            Expression e = new ArithmeticExpression(
                                p1.reduce(dependencies, context),
                                operator,
                                p2.reduce(dependencies, context));
            e.setStaticContext(getStaticContext());
            return e.simplify();
        } else {
            return this;
        }
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
