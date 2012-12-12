package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.lang.Math;
import java.text.*;



public class Lang extends Function {

    public String getName()      { return "lang"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getIntrinsicDependencies() {
        return Context.CONTEXT_NODE;    
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(1, 1);

        Value arg0 = (Value)args.elementAt(0);
        String arglang = arg0.asString();
        return new BooleanValue(isLang(arglang, context));
    }

    /**
    * Test whether the context node has the given language attribute
    * @param arglang the language being tested
    * @param context the context, to identify the context node
    */

    public static boolean isLang(String arglang, Context context) throws SAXException {

        NodeInfo node = context.getContextNode();
       
        Name xmllang = Name.reconstruct("xml", Namespace.XML, "lang");
        String doclang=null;
        
        while(node!=null) {
            doclang = node.getAttributeValue(xmllang);
            if (doclang!=null) break;
            node=(NodeInfo)node.getParentNode();
        }

        if (doclang==null) return false;
        
        if (arglang.equalsIgnoreCase(doclang)) return true;
        int hyphen = doclang.indexOf("-");
        if (hyphen<0) return false;
        doclang = doclang.substring(0, hyphen);
        if (arglang.equalsIgnoreCase(doclang)) return true;
        return false;
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
