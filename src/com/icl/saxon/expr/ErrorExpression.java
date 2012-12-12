package com.icl.saxon.expr;
import com.icl.saxon.*;
import java.util.*;
import org.xml.sax.SAXException;

/**
* Error expression: this expression is generated when the supplied expression cannot be
* parsed, and the containing element enables forwards-compatible processing. It defers
* the generation of an error message until an attempt is made to evaluate the expression
*/

class ErrorExpression extends Expression {

    private SAXException exception;     // the error found when parsing this expression

    /**
    * Constructor
    * @param exception the error found when parsing this expression
    */

    public ErrorExpression(SAXException exception) {
        this.exception = exception;
    };

    /**
    * Evaluate this expression. This always throws the exception registered when the expression
    * was first parsed.
    */

    public Value evaluate(Context c) throws SAXException {
        throw exception;
    }

    /**
    * Determine the data type of the expression, if possible
    * @return Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        return Value.ANY;
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
