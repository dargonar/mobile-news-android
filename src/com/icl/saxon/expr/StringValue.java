package com.icl.saxon.expr;
import com.icl.saxon.*;
import org.xml.sax.SAXException;

/**
* A string value
*/

public final class StringValue extends Value {
    private String value;   // may be zero-length, will never be null

    /**
    * Constructor
    * @param value the String value. Null is taken as equivalent to "".
    */

    public StringValue(String value) {
        this.value = (value==null ? "" : value);
    }

    /**
    * Get the string value as a String
    */
    
    public String asString() {
        return value;
    }

    /**
    * Convert the string value to a number
    */

    public double asNumber() {
        return Value.stringToNumber(value);
    }

    /**
    * Convert the string value to a boolean
    * @return false if the string value is zero-length, true otherwise
    */

    public boolean asBoolean() {
        return (value.length()>0);
    }

    /**
    * Return the type of the expression (if known)
    * @return Value.STRING (always)
    */

    public int getType() {
        return Value.STRING;
    }

    /**
    * Get the length of this string, as defined in XPath. This is not the same as the Java length,
    * as a Unicode surrogate pair counts as a single character
    */

    public int getLength() {
        return getLength(value);
    }

    /**
    * Get the length of a string, as defined in XPath. This is not the same as the Java length,
    * as a Unicode surrogate pair counts as a single character.
    * @param s The string whose length is required
    */

    public static int getLength(String s) {
        int n = 0;
        for (int i=0; i<s.length(); i++) {
            int c = (int)s.charAt(i);
            if (c<55296 || c>56319) n++;    // don't count high surrogates, i.e. D800 to DBFF
        }
        return n;
    }

    /**
    * Expand a string containing surrogate pairs into an array of 32-bit characters
    */

    public static int[] expand(String s) {
        int[] array = new int[getLength(s)];
        int o=0;
        for (int i=0; i<s.length(); i++) {
            int charval;
            int c = s.charAt(i);
            if (c>=55296 && c<=56319) {
                // we'll trust the data to be sound
                charval = ((c - 55296) * 1024) + ((int)s.charAt(i+1) - 56320) + 65536;
                i++;
            } else {
                charval = c;
            }
            array[o++] = charval;
        }        
        return array;
    }

    /**
    * Determine if two StringValues are equal
    */

    public boolean equals(StringValue other) {
        return this.value.equals(other.value);
    }

    /**
    * Get a diagnostic string representation of the value (a string literal in quotes)
    */

    public String toString() {
        return "\"" + asString() + "\"";
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

