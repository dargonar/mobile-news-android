package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;

import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;

/**
* Handler for xsl:variable elements in stylesheet.<BR>
* The xsl:variable element has mandatory attribute name and optional attribute select
*/

public class XSLVariable extends XSLGeneralVariable implements Binding {

    private int slotNumber;

    public int getSlotNumber() {
        return slotNumber;
    }

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction (well, it can be, anyway)
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Check that the variable is not already declared, and allocate a slot number
    */

    public void validate() throws SAXException {
        super.validate();
        checkDuplicateDeclaration();
        if (global) {
            slotNumber = getPrincipalStyleSheet().allocateSlotNumber();
        } else {
            Procedure p = getOwningProcedure();
            slotNumber = p.allocateSlotNumber();
        }
    }

    /**
    * Get the data type, if known statically.
    * @return the data type if known
    */
    
    public int getDataType() {
        if (assignable) {
            return Value.ANY;
        }
        if (select!=null) {
            return select.getDataType();
        } else {
            return Value.NODESET;
        }
    }

    /**
    * Get the value, if known statically.
    * @return null, because the value of a parameter is never known in advance
    */
    
    public Value constantValue() {
        if (assignable) {
            return null;
        }
        if (select!=null && select instanceof Value) {
            return (Value)select;
        } else {
            return null;
        }
    }


    /**
    * Process the variable declaration
    */

    public void process( Context context ) throws SAXException
    {
        Bindery b = context.getBindery();
        if (global) {
            if (!b.isEvaluated(this)) {                 // don't evaluate a global variable twice
                Value value = getSelectValue(context);
                b.defineGlobalVariable(this, value);
            } 
        } else {
            Value value = getSelectValue(context);
            b.defineLocalVariable(this, value);
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
