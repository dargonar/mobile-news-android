package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.lang.Math;

public class Substring extends Function {

    public String getName()      { return "substring"; };

    /**
    * Determine the data type of the expression, if possible
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Evaluate the function
    */

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(2, 3);
        
        Value arg0 = (Value)args.elementAt(0);
        String s = arg0.asString();
        
        double arg1 = ((Value)args.elementAt(1)).asNumber();

        if (numArgs==2) {
            return new StringValue(substring(s, arg1));
        } else {
            double arg2 = ((Value)args.elementAt(2)).asNumber();
            return new StringValue(substring(s, arg1, arg2));
        }
    }
    
    /**
    * Simplify
    * This is a pure function so it can be simplified in advance if the arguments are known
    */

    public Expression simplify() throws SAXException {
        return simplifyPureFunction();
    }

    /**
    * Implement substring function. This follows the algorithm in the spec precisely.
    */

    public static String substring(String s, double start) {
        int slength = s.length();
        int estlength = slength - (int)start+1;
        if (estlength < 0) estlength = 1;
        if (estlength > slength) estlength = slength;
        StringBuffer sb = new StringBuffer(estlength);
        int pos=1;
        int cpos=0;
        double rstart = Round.round(start);

        while (cpos<slength) {
            if (pos >= rstart) {
                sb.append(s.charAt(cpos));
            }

            int ch = (int)s.charAt(cpos++);
            if (ch<55296 || ch>56319) pos++;    // don't count high surrogates, i.e. D800 to DBFF
        }
        return sb.toString();
    }

    /**
    * Implement substring function. This follows the algorithm in the spec precisely, except that
    * we exit the loop once we've exceeded the required length.
    */

    public static String substring(String s, double start, double len) {
        int slength = s.length();
        int estlength = (int)len;
        if (estlength < 0) estlength = 1;
        if (estlength > slength) estlength = slength;

        StringBuffer sb = new StringBuffer(estlength);
        int pos=1;
        int cpos=0;
        double rstart = Round.round(start);
        double rlen = Round.round(len);

        while (cpos<slength) {
            if (pos >= rstart) {
                if (pos < rstart + rlen) {
                    sb.append(s.charAt(cpos));
                } else {
                    break;
                }
            }

            int ch = (int)s.charAt(cpos++);
            if (ch<55296 || ch>56319) pos++;    // don't count high surrogates, i.e. D800 to DBFF
        }

        return sb.toString();
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
