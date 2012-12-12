package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;


public class Sum extends Function {

    public String getName()      { return "sum"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.NUMBER
    */

    public int getDataType() {
        return Value.NUMBER;
    }

    public boolean isNumeric() {
        return true;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(1, 1);
        Value arg0 = (Value)args.elementAt(0);
        if (!(arg0 instanceof NodeSetValue)) {
            throw new SAXException("Argument to sum() is not a node set");
        }
        return new NumericValue(total((NodeSetValue)arg0));
    }

    public static double total(NodeSetValue nodeset) throws SAXException {
        NodeInfo[] v = nodeset.getNodes();
        double sum = 0.0;
        for (int i=0; i<v.length; i++) {
            String val = v[i].getValue();
            sum += Value.stringToNumber(val);
        }
        return sum;
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
