package com.icl.saxon;
import com.icl.saxon.expr.Value;
import org.xml.sax.SAXException;

/**
* Binding is a interface used to mark objects that represent a variable declaration
*
*/

public interface Binding  {

    /**
    * Determine whether this variable is global
    */

    public boolean isGlobal() throws SAXException;
 
    /**
    * Establish the name of this variable. The string is the absolute form of the name,
    * as returned by Name.getAbsoluteName().
    */

    public String getVariableName() throws SAXException;

    /**
    * Determine a slot number for the variable. 
    */

    public int getSlotNumber();

    /**
    * Get the data type, if known statically. This will be a value such as Value.BOOLEAN,
    * Value.STRING. If the data type is not known statically, return Value.ANY.
    */

    public int getDataType();

    /**
    * Get the value of the variable, if known statically. If the value is not known statically,
    * return null.
    */

    public Value constantValue();

    /**
    * Determine whether the variable is assignable using saxon:assign
    */

    public boolean isAssignable();
    
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
