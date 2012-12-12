package com.icl.saxon.expr;
import com.icl.saxon.*;
import org.xml.sax.SAXException;

/**
* A boolean XPath value
*/

public final class BooleanValue extends Value {
    private boolean value;

    /**
    * Constructor: create a boolean value
    * @param value the initial value, true or false
    */

    public BooleanValue(boolean value) {
        this.value = value;
    }

    /**
    * Convert to string
    * @return "true" or "false"
    */

    public String asString() {
        return (value ? "true" : "false");
    }

    /**
    * Convert to number
    * @return 1 for true, 0 for false
    */

    public double asNumber() {
        return (value ? 1 : 0);
    }

    /**
    * Convert to boolean (null operation)
    * @return the value
    */

    public boolean asBoolean() {
        return value;
    }
    
    /**
    * Get string representation
    * @return "true" or "false"
    */
    
    public String toString() {
        return asString();
    }

    /**
    * Determine the data type of the exprssion
    * @return Value.BOOLEAN,
    */

    public int getDataType() {
        return Value.BOOLEAN;
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

