package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;

/**
* saxon:assign element in stylesheet.<BR>
* The saxon:assign element has mandatory attribute name and optional attribute expr
*/

public class SAXONAssign extends XSLGeneralVariable {

    private Binding binding;    // link to the variable declaration

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void validate() throws SAXException {
        checkWithinTemplate();
        super.validate();
        binding = bindVariable(getVariableName());
        if (!binding.isAssignable()) {
            throw new SAXException("Variable " + getVariableName() + " is not marked as assignable");
        }
    }

    public void process( Context context ) throws SAXException
    {            
        Value value = getSelectValue(context);    
        context.getController().getBindery().assignVariable(binding, value);
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
