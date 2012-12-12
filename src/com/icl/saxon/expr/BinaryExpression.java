package com.icl.saxon.expr;
import com.icl.saxon.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* Binary Expression: a numeric expression consisting of the two operands and an operator
*/

abstract class BinaryExpression extends Expression {

    protected Expression p1, p2;
    protected int operator;       // represented by the token number from class Tokenizer

    /**
    * Default constructor
    */

    public BinaryExpression() {}

    /**
    * Create a binary expression identifying the two operands and the operator
    * @param p1 the left-hand operand
    * @param op the operator, as a token returned by the Tokenizer (e.g. Tokenizer.AND)
    * @param p2 the right-hand operand
    */

    public BinaryExpression(Expression p1, int op, Expression p2) {
            this.p1 = p1;
            this.p2 = p2;
            this.operator = op;
    }

    /**
    * Identify the two operands and the operator (for use when the default constructor was used)
    * @param p1 the left-hand operand
    * @param op the operator, as a token returned by the Tokenizer (e.g. Tokenizer.AND)
    * @param p2 the right-hand operand
    */

    public void setDetails(Expression p1, int op, Expression p2) {
            this.p1 = p1;
            this.p2 = p2;
            this.operator = op;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        p1 = p1.simplify();
        p2 = p2.simplify();

        // if both operands are known, pre-evaluate the expression
        if ((p1 instanceof Value) && (p2 instanceof Value)) {
            return evaluate(null);
        }
        return this;
    }

    /**
    * Return the text of the expression as a string.
    */

    public String toString() {
        return "(" + p1.toString() +
                 " " + Tokenizer.tokens[operator] + " " +
                 p2.toString() + ")";
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return p1.getDependencies() | p2.getDependencies();
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
