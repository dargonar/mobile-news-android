package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.trace.*;  // e.g.
import com.icl.saxon.style.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* Variable reference: a reference to an XSL variable
*/

public class VariableReference extends Expression {

    private Name fullname;
    private Binding binding;
    
    /**
    * Constructor
    * @param name the variable name (as a Name object)
    */

    public VariableReference(Name name, StaticContext staticContext) throws SAXException {
        this.fullname = name;
        this.binding = staticContext.bindVariable(name.getAbsoluteName());
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return Context.VARIABLES;
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
        if ((dependencies & Context.VARIABLES) != 0) {
            return evaluate(context);
        } else {
            return this;
        }
    }

    /**
    * Get the value of this variable in a given context.
    * @param context the Context which contains the relevant variable bindings
    * @return the value of the variable, if it is defined
    * @throw SAXException if the variable is undefined
    */


    public Value evaluate(Context c) throws SAXException {

        Bindery b = c.getBindery();
        Value v = b.getValue(binding);
        
        if (v==null) {

            if (!binding.isGlobal()) {
                throw new SAXException("Variable " + fullname.getDisplayName() + " is undefined");
            }
            
            // it must be a forwards reference; try to evaluate it now.
            // but first set a flag to stop looping. This flag is set in the Bindery because
            // the VariableReference itself can be used by multiple threads simultaneously

            b.setExecuting(binding, true);

            if (binding instanceof XSLGeneralVariable) {
        		if (c.getController().isTracing()) { // e.g.
        		    TraceListener listener = c.getController().getTraceListener();
		    
        		    listener.enter((XSLGeneralVariable)binding, c);
        		    ((XSLGeneralVariable)binding).process(c);
        		    listener.leave((XSLGeneralVariable)binding, c);

        		} else {
        		    ((XSLGeneralVariable)binding).process(c);
        		}
            }

            b.setExecuting(binding, false);
            
            v = b.getValue(binding);
            
            if (v==null) {
                throw new SAXException("Variable " + fullname.getDisplayName() + " is undefined");
            }
        }
        return v;
    }

    /**
    * Get the object bound to the variable
    */

    public Binding getBinding() {
        return binding;
    }

    /**
    * Get a string representing the variable reference, for diagnostics
    */

    public String toString() {
        return "$" + fullname.getDisplayName();
    }

    /**
    * Determine the data type of the expression, if possible
    * @return the type of the variable, if this can be determined statically;
    * otherwise Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        return binding.getDataType();
    }

    /**
    * Simplify the expression. If the variable has a fixed value, the variable reference
    * will be replaced with that value.
    */

    public Expression simplify() {
        Value v = binding.constantValue();
        if (v==null) {
            return this;
        } else {
            return v;
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
// Contributor(s): 
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//
