package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.lang.Math;
import java.text.*;



public class Translate extends Function {

    public String getName()      { return "translate"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(3, 3);

        Value arg0 = (Value)args.elementAt(0);
        Value arg1 = (Value)args.elementAt(1);
        Value arg2 = (Value)args.elementAt(2);
        String s0 = arg0.asString();
        String s1 = arg1.asString();        
        String s2 = arg2.asString();
        
        return new StringValue(translate(s0, s1, s2));
    }

    /**
    * Perform the translate function
    */

    public static String translate(String s0, String s1, String s2) {

        // check for surrogate pairs
        int len0 = StringValue.getLength(s0);
        int len1 = StringValue.getLength(s1);        
        int len2 = StringValue.getLength(s2);
        if (s0.length()!=len0 ||
                s1.length()!=len1 ||
                s2.length()!=len2 ) {
            return slowTranslate(s0, s1, s2);
        }

        StringBuffer sb = new StringBuffer();
        int s2len = s2.length();
        for (int i=0; i<s0.length(); i++) {
            char c = s0.charAt(i);
            int j = s1.indexOf(c);
            if (j<s2len) {
                sb.append(( j<0 ? c : s2.charAt(j) ));
            }
        }
        return sb.toString();
    }

    /**
    * Perform the translate function when surrogate pairs are in use
    */

    private static String slowTranslate(String s0, String s1, String s2) {
        int[] a0 = StringValue.expand(s0);
        int[] a1 = StringValue.expand(s1);
        int[] a2 = StringValue.expand(s2);
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<a0.length; i++) {
            int c = a0[i];
            int j = -1;
            for (int test=0; test<a1.length; test++) {
                if (a1[test]==c) {
                    j = test;
                    break;
                }
            }
            int newchar = -1;
            if (j<0) {
                newchar = a0[i];
            } else if (j<a2.length) {
                newchar = a2[j];
            } else {
                // no new character
            }

            if (newchar>=0) {
                if (newchar<65536) {
                    sb.append((char)newchar);
                }
                else {  // output a surrogate pair
                    //To compute the numeric value of the character corresponding to a surrogate
                    //pair, use this formula (all numbers are hex):
            	    //(FirstChar - D800) * 400 + (SecondChar - DC00) + 10000
                    newchar -= 65536;
                    sb.append((char)((newchar / 1024) + 55296));
                    sb.append((char)((newchar % 1024) + 56320));
                }
            }
        }
        return sb.toString();
    }


    
    /**
    * Simplify
    * This is a pure function so it can be simplified in advance if the arguments are known
    */

    public Expression simplify() throws SAXException {
        return simplifyPureFunction();
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
