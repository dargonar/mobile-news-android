package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.functions.Concat;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;
import java.util.*;

/**
* This class defines common behaviour across xsl:variable, xsl:param, and xsl:with-param
*/

public abstract class XSLGeneralVariable extends StyleElement  {

    protected String variableName = null;
    protected Expression select = null;
    protected boolean global;
    protected Procedure procedure=null;  // used only for global variables
    protected boolean assignable=false;

    private Name saxonAssignable = Name.reconstruct("saxon", Namespace.SAXON, "assignable");

    public boolean isGlobal() throws SAXException {
        return (getParentNode() instanceof XSLStyleSheet);
    }

    /**
    * Test whether it is permitted to assign to the variable using the saxon:assign
    * extension element. This will only be true if the extra attribute saxon:assignable="yes"
    * is present.
    */

    public boolean isAssignable() {
        return assignable;
    }
        
    /**
    * Get the owning Procedure definition, if this is a local variable
    */

    public Procedure getOwningProcedure() throws SAXException {
        NodeInfo node = this;
        while (true) {
            NodeInfo next = (NodeInfo)node.getParentNode();
            if (next instanceof XSLStyleSheet) {
                if (node instanceof XSLTemplate) {
                    return ((XSLTemplate)node).getProcedure();
                } else if (node instanceof XSLGeneralVariable) {
                    return ((XSLGeneralVariable)node).getProcedure();
                } else if (node instanceof SAXONFunction) {
                    return ((SAXONFunction)node).getProcedure();
                } else {
                    throw new SAXException("Local variable must be within xsl:template or xsl:variable");
                }
            }
            node=next;
        }
    }

    /**
    * Preprocess: this ensures space is available for local variables declared within
    * this global variable
    */

    public void preprocess() throws SAXException
    {
        if (global) {
            getPrincipalStyleSheet().allocateLocalSlots(procedure.getNumberOfVariables());
        }     
    }

    /**
    * Get the absolute (namespace-qualified) name of the variable.
    */

    public String getVariableName() throws SAXException {
        
        // if an expression has a forwards reference to this variable, getVariableName() can be
        // called before prepareAttributes() is called. We need to allow for this.
        
        if (variableName==null) {
            String nameAttribute = getAttributeValue("name");
            if (nameAttribute==null) {
                reportAbsence("name");
            }
            variableName = (new Name(nameAttribute, this, false)).getAbsoluteName();
        }
        return variableName;
    }

    public void prepareAttributes() throws SAXException {
        
        String[] allowed = {"name", "select"};
        allowAttributes(allowed);

        getVariableName();

        String exprAtt = getAttributeValue("select");
        if (exprAtt!=null) {
            select = Expression.make(exprAtt, this);
        }

        String assignAtt = getAttributeValue(saxonAssignable);
        if (assignAtt!=null && assignAtt.equals("yes")) {
            assignable=true;
        }
    }

    public void validate() throws SAXException {
        global = (getParentNode() instanceof XSLStyleSheet);
        if (global) {
            procedure = new Procedure();
        }
        if (select!=null && getFirstChild()!=null) {
            throw styleError("An " + getDisplayName() + " element with a select attribute must be empty");
        }

        if (select==null) {
            if (getNumberOfChildren()==0) {
                select = new StringValue("");
            }
        }                          
    }

    /**
    * Check whether this declaration duplicates another one
    */

    public void checkDuplicateDeclaration() throws SAXException {
        Binding binding = getVariableBinding(getVariableName());
        if (binding!=null) {
            if (global) {
                if (((XSLGeneralVariable)binding).getPrecedence()==this.getPrecedence()) {
                    throw styleError("Duplicate global variable declaration");
                }
            } else {
                if (!binding.isGlobal()) {
                    //System.err.println("Clash with line " + ((StyleElement)binding).getLineNumber());
                    throw styleError("Variable is already declared in this template");
                }
            }
        }
    }


    /**
    * Get the value of the select expression if present or the content of the element otherwise
    */

    protected Value getSelectValue(Context context) throws SAXException {
        if (select==null) {
            FragmentValue frag = new FragmentValue();
            Controller c = context.getController();
            OutputDetails details = new OutputDetails();
            details.setMethod("saxon:fragment");
            details.setEmitter(frag.getEmitter());
            c.setNewOutputDetails(details);
            if (global && procedure.getNumberOfVariables()>0) {
                Bindery bindery = context.getBindery();
                bindery.openStackFrame(new ParameterSet());
                processChildren(context);
                bindery.closeStackFrame();
            } else {
                processChildren(context);
            }
            c.resetOutputDetails();
            frag.setBaseURI(getSystemId());
            return frag;

        } else {
            return select.evaluate(context);
        }
    }

    /**
    * Get associated Procedure (for details of stack frame, if this is a global variable containing
    * local variable declarations)
    */

    public Procedure getProcedure() {
        return procedure;
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
