package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.functions.*;
import org.xml.sax.SAXException;
import java.text.*;

/**
* An XPath value that encapsulates a Java object. Such a value can only be obtained by
* calling an extension function that returns it.
*/

public class ObjectValue extends Value {
    private Object value;

    /**
    * Constructor 
    * @value the object to be encapsulated
    */

    public ObjectValue(Object object) {
        this.value = object;
    }

    /**
    * Get the value as a String
    * @return a String representation of the value
    */

    public String asString() {
        return (value==null ? "" : value.toString());
    }

    /**
    * Get the value as a number
    * @return the numeric value
    */

    public double asNumber() {
        return (value==null ? Double.NaN : Value.stringToNumber(value.toString()));
    }

    /**
    * Convert the value to a boolean
    * @return the boolean value
    */

    public boolean asBoolean() {
        return (value==null ? false : value.toString().length() > 0);
    }

    /**
    * Determine the data type of the expression
    * @return Value.OBJECT
    */

    public int getDataType() {
        return Value.OBJECT;
    }

    /**
    * Determine whether the expression is numeric
    * @return false
    */

    public boolean isNumeric() {
        return false;
    }

    /**
    * Get the encapsulated object
    */

    public Object getObject() {
        return value;
    }

    /**
    * Return a string representation for diagnostics
    */

    public String toString() {
        return (value==null ? "#null#" : asString());
    }

    /**
    * Determine if two ObjectValues are equal
    */

    public boolean equals(ObjectValue other) {
        return this.value.equals(other.value);
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

