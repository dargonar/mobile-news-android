package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.functions.*;
import org.xml.sax.SAXException;
import java.text.*;

/**
* A numeric (floating point) value
*/

public final class NumericValue extends Value {
    private double value;

    /**
    * Constructor supplying a double
    * @value the value of the NumericValue
    */

    public NumericValue(double value) {
        this.value = value;
    }

    /**
    * Constructor supplying a String
    * @s the numeric value expressed as a String
    */

    public NumericValue(String s) {
        this.value = Value.stringToNumber(s);
    }

    /**
    * Get the value as a String
    * @return a String representation of the value
    */

    // Algorithm used up to 5.3.1

    public String asStringOLD() {
        if (Double.isNaN(value)) return "NaN";
        if (Double.isInfinite(value)) return (value>0 ? "Infinity" : "-Infinity");
        if (value==0.0) return "0";
     
        double absvalue = Math.abs(value);
        StringBuffer sb = new StringBuffer();
        if (value<0) sb.append('-');
        int offset = (value<0 ? 1: 0);
        double intpart = Math.floor(absvalue);
        double fraction = absvalue - intpart;
        if (intpart>=1) {
            while (intpart>=1) {
                int nextdigit = (int)(intpart % 10);
                char digit = (char)(nextdigit + '0');
                sb.insert(offset, digit);                
                intpart = Math.floor(intpart / 10);
            }
            
        } else {
            sb.append('0');
        }
        if (fraction > 0) {
            sb.append('.');
            while (fraction > 0) {
                double next = fraction * 10;
                if (next<1.000000000001 && next>0.999999999999) next=1.0;
                double nextdigit = Math.floor(next);
                char digit = (char)((int)nextdigit + '0');
                sb.append(digit);
                fraction = next % 1.0;
            }
        }
        return sb.toString();
    }

    /**
    * Get the value as a String
    * @return a String representation of the value
    */

    // Code copied from James Clark's xt
    
    public String asString() {
        if (!Double.isInfinite(value)
	        && (value >= (double)(1L << 53)
	                || -value >= (double)(1L << 53))) {
            return new java.math.BigDecimal(value).toString();
        }
        String s = Double.toString(value);
        int len = s.length();
        if (s.charAt(len - 2) == '.' && s.charAt(len - 1) == '0') {
            s = s.substring(0, len - 2);
            if (s.equals("-0"))
                return "0";
            return s;
        }
        int e = s.indexOf('E');
        if (e < 0)
            return s;
        int exp = Integer.parseInt(s.substring(e + 1));
        String sign;
        if (s.charAt(0) == '-') {
            sign = "-";
            s = s.substring(1);
            --e;
        }
        else
            sign = "";
        int nDigits = e - 2;
        if (exp >= nDigits)
            return sign + s.substring(0, 1) + s.substring(2, e) + zeros(exp - nDigits);
        if (exp > 0)
            return sign + s.substring(0, 1) + s.substring(2, 2 + exp) + "." + s.substring(2 + exp, e);
        return sign + "0." + zeros(-1 - exp) + s.substring(0, 1) + s.substring(2, e);
    }
    
    static private String zeros(int n) {
        char[] buf = new char[n];
        for (int i = 0; i < n; i++)
            buf[i] = '0';
        return new String(buf);
    }

    /**
    * Get the value as a number
    * @return the numeric value
    */

    public double asNumber() {
        return value;
    }

    /**
    * Convert the value to a boolean
    * @return false if zero, true otherwise
    */

    public boolean asBoolean() {
        return (value!=0.0 && !Double.isNaN(value));
    }

    /**
    * Determine the data type of the exprssion, if possible
    * @return one of the values Value.STRING, Value.BOOLEAN, Value.NUMBER, Value.NODESET,
    * Value.FRAGMENT, or Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        return Value.NUMBER;
    }

    public boolean isNumeric() {
        return true;
    }

    /**
    * Return a string representation for diagnostics
    */

    public String toString() {
        return asString();
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
// The Original Code is: all this file except the asStringXT() and zeros() methods (not currently used). 
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// Portions created by (xt) are Copyright (C) (James Clark). All Rights Reserved. 
//
// Contributor(s): none. 
//

