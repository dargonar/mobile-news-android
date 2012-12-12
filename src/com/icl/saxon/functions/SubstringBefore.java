package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;



public class SubstringBefore extends Function {

    public String getName()      { return "substring-before"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(2, 2);
        Value arg0 = (Value)args.elementAt(0);
        Value arg1 = (Value)args.elementAt(1);
        String s1 = arg0.asString();
        String s2 = arg1.asString();

        return new StringValue(before(s1, s2));
    }

    public static String before(String s1, String s2) {
        int i = s1.indexOf(s2);
        if (i<0) return "";
        return s1.substring(0, i);
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
