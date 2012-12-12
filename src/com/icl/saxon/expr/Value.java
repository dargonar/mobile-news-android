package com.icl.saxon.expr;
import com.icl.saxon.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A value is the result of an expression but it is also an expression in its own right
*/

public abstract class Value extends Expression {

    /**
    * Static method to convert strings to numbers. Might as well go here as anywhere else.
    * @param s the String to be converted
    * @return a double representing the value of the String; if it cannot be converted,
    * return NaN (as required by the XSL specification)
    */

    public static double stringToNumber(String s) {
        try {
            return new Double(s.trim()).doubleValue();
        } catch (NumberFormatException err) {
            return Double.NaN;
        }
    }

    /**
    * Constants denoting the data types of an expression or value
    */

    public static final int BOOLEAN = 1;
    public static final int NUMBER = 2;
    public static final int STRING = 3;
    public static final int NODESET = 4;
    //public static final int FRAGMENT = 5;
    public static final int OBJECT = 6;
    public static final int ANY = -1;
    
    /**
    * Evaluate the Value. Null operation, because it has already been evaluated
    * @param context The context (not used)
    * @return the value, unchanged
    */

    public Value evaluate(Context context) throws SAXException {
        return this;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        return this;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return 0;
    }

    /**
    * Convert the value to a String value
    * @return the value converted to a String
    */

    public abstract String asString() throws SAXException;

    /**
    * Convert the value to a Number
    * @return the value converted to a String
    */

    public abstract double asNumber() throws SAXException ;

    /**
    * Convert the value to a Boolean
    * @return the value converted to a Boolean
    */

    public abstract boolean asBoolean() throws SAXException ;

    /**
    * Test whether two values are equal. See the XSL specification: if either operand is a
    * nodeset, they are compared as nodesets; else if either is a boolean, they
    * are compared as booleans; else if either operand is a number, they are compared as numbers;
    * else they are compared as strings.
    * @return a boolean giving the value of the expression, evaluated in the current context
    */

    public boolean equals(Value other) throws SAXException {

        // if this is a NodeSet value, the method will be handled by the NodeSetValue class
        
        if (other instanceof NodeSetValue)
            return other.equals(this);
            
        if (this instanceof BooleanValue || other instanceof BooleanValue)
            return this.asBoolean() == other.asBoolean();
            
        if (this instanceof NumericValue || other instanceof NumericValue)
            return this.asNumber() == other.asNumber();
                    
        return this.asString().equals(other.asString()); 

    }

    /**
    * Test whether two values are not-equal. Note that a!=b means the same as !(a=b) except
    * where either a or b is a nodeset.
    * @return a boolean giving the value of the expression, evaluated in the current context
    */

    public boolean notEquals(Value other) throws SAXException {

        // if this is a NodeSet value, the method will be handled by the NodeSetValue class
        
        if (other instanceof NodeSetValue)
            return other.notEquals(this);

        return !equals(other);
    }

    /**
    * Test how a Value compares to another Value under a relational comparison.
    * Note that the method is overridden for NodeSetValue
    * @param operator The comparison operator, one of Tokenizer.LE, Tokenizer.LT,
    * Tokenizer.GE, Tokenizer.GT, Tokenizer.EQUALS, Tokenizer.NE. 
    */

    public boolean compare(int operator, Value other) throws SAXException {

        if (operator==Tokenizer.EQUALS) return equals(other);
        if (operator==Tokenizer.NE) return notEquals(other);

        if (other instanceof NodeSetValue) {
            return other.compare(inverse(operator), this);
        }

        return numericCompare(operator, this.asNumber(), other.asNumber());
    }

    protected final int inverse(int operator) {
        switch(operator) {
            case Tokenizer.LT:
                return Tokenizer.GT;
            case Tokenizer.LE:
                return Tokenizer.GE;                    
            case Tokenizer.GT:
                return Tokenizer.LT;
            case Tokenizer.GE:
                return Tokenizer.LE;
            default:
                return operator;
        }
    }
    

    protected final boolean numericCompare(int operator, double x, double y) {
        switch(operator) {
            case Tokenizer.LT:
                return x < y;
            case Tokenizer.LE:
                return x <= y;                    
            case Tokenizer.GT:
                return x > y;
            case Tokenizer.GE:
                return x >= y;
            default:
                return false;
        }
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
        return this;
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
