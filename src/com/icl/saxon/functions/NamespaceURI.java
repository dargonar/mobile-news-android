package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.Name;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;


public class NamespaceURI extends Function {

    public String getName()      { return "namespace-uri"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getIntrinsicDependencies() {
        int numArgs = arguments.size();
        return (numArgs==0 ? Context.CONTEXT_NODE : 0);
    }

    /**
    * Remove intrinsic dependencies. 
    */

    protected Expression reduceIntrinsic(int dependencies, Context context)
            throws SAXException {
        int numArgs = checkArgumentCount(0, 1);
        if (numArgs==0 && ((dependencies & Context.CONTEXT_NODE)!=0)) {
            return evaluate(context);
        } else {
            return this;
        }
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(0, 1);
        NodeInfo node;        
        if (numArgs==0) {
            node = context.getContextNode();
        } else {
            Value arg0 = (Value)args.elementAt(0);
            node = ((NodeSetValue)arg0).getFirst();
        }
        
        if (node==null) {
            return new StringValue("");
        }
        Name name = node.getExpandedName();
        if (name==null) {
            return new StringValue("");
        }
        return new StringValue(name.getURI());
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
