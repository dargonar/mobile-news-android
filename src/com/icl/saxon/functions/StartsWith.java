package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.text.*;



public class StartsWith extends Function {

    public String getName()      { return "starts-with"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(2, 2);
        Value arg0 = (Value)args.elementAt(0);
        Value arg1 = (Value)args.elementAt(1);
        return new BooleanValue(arg0.asString().startsWith(arg1.asString()));
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
