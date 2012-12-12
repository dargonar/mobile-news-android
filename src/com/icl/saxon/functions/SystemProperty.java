package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.lang.Math;
import java.text.*;



public class SystemProperty extends Function {

    public String getName()      { return "system-property"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.ANY
    */

    public int getDataType() {
        return Value.ANY;
    }


    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(1, 1);
        Value arg0 = (Value)args.elementAt(0);
        String name = arg0.asString();
        Name fullname = getStaticContext().makeName(name, false);
        return getProperty(fullname);
    }

    public static Value getProperty(Name fullname) {
        String uri = fullname.getURI();
        String local = fullname.getLocalName();
        if (uri == Namespace.XSLT) {
            if (local.equals("version"))
                return new NumericValue(Version.getXSLVersion());
            if (local.equals("vendor"))
                return new StringValue(Version.getProductName());
            if (local.equals("vendor-url"))
                return new StringValue(Version.getWebSiteAddress());
        }
            
        String val = System.getProperty(local);
        if (val==null) val="";
        return new StringValue(val);
    }

    /**
    * getProperty by absolute name: interface used by the compiler
    */

    public static Value getProperty(String absoluteName) throws SAXException {
        int hat = absoluteName.indexOf("^");
        String uri;
        String localName;
        if (hat<0) {
            uri = "";
            localName = absoluteName;
        } else {
            uri = absoluteName.substring(0, hat);
            localName = absoluteName.substring(hat+1);
        }
        return getProperty(new Name("prefix", uri, localName));
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
