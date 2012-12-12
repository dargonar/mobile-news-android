package com.icl.saxon.expr;
import com.icl.saxon.*;
import org.xml.sax.SAXException;

/**
* Relational Expression: a boolean expression that compares two expressions
* for equals, not-equals, greater-than or less-than.
*/

class RelationalExpression extends BinaryExpression {

    /**
    * Default constructor
    */

    public RelationalExpression(){};

    /**
    * Create a relational expression identifying the two operands and the operator
    * @param p1 the left-hand operand
    * @param op the operator, as a token returned by the Tokenizer (e.g. Tokenizer.LT)
    * @param p2 the right-hand operand
    */

    public RelationalExpression(Expression p1, int op, Expression p2) {
        super(p1, op, p2);
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {

        // detect common case such as @att='x'
        
        p1 = p1.simplify();
        p2 = p2.simplify();
        if (p1 instanceof SingletonExpression && (
                p2 instanceof StringValue ||
                p2 instanceof NumericValue)) {
            return new SingletonComparison((SingletonExpression)p1, operator, (Value)p2);
        }
        return super.simplify();
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a BooleanValue representing the result of the numeric comparison of the two operands
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
        Value s1 = p1.evaluate(c);
        Value s2 = p2.evaluate(c);
        return s1.compare(operator, s2);
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
            Expression e = new RelationalExpression(
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
