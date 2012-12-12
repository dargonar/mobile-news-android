package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;


public class FunctionAvailable extends Function {

    public String getName()      { return "function-available"; };

    /**
    * Determine the data type of the expression, if possible
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(1, 1);

        String arg0 = ((Value)args.elementAt(0)).asString();

        // Return true for a recognised system function name
        
        if (arg0.indexOf(':')<0) {
            return(new BooleanValue(ExpressionParser.makeSystemFunction(arg0)!=null));
        }

        Name fullname = getStaticContext().makeName(arg0, false);

        // See if the function is defined in the stylesheet

        if (getStaticContext().getStyleSheetFunction(fullname)!=null) {
            return new BooleanValue(true);
        }

        // See if there is a Java external function

        return new BooleanValue((new FunctionProxy()).isAvailable(fullname));
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
