package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.expr.*;
import com.icl.saxon.style.*;
import org.xml.sax.SAXException;
import java.util.*;


public class ElementAvailable extends Function {

    public String getName()      { return "element-available"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(1, 1);

        Value arg0 = (Value)args.elementAt(0);
        String name = arg0.asString();

        try {
            Name fullName = getStaticContext().makeName(name, true);
            String uri = fullName.getURI();
            String localname = fullName.getLocalName();

            if (uri.equals(Namespace.XSLT)) {
                // return true for any XSLT element
                // TODO: only return true for an *instruction*
                boolean yes = (new StyleNodeFactory()).isXSLElement(localname);
                return new BooleanValue(yes);
            } else if (uri.equals(Namespace.SAXON)) {
                boolean yes = (new StyleNodeFactory()).isSAXONElement(localname);
                return new BooleanValue(yes);
            } else {
                ExtensionElementFactory factory = getFactory(uri);
                if (factory==null) return new BooleanValue(false);     
                Class c = factory.getExtensionClass(localname);
                if (c==null) return new BooleanValue(false);     
                StyleElement node = (StyleElement)c.newInstance();
                return new BooleanValue(true);
            }
        } catch (Exception err) {
            return new BooleanValue(false);
        }
    }

    /**
    * Get the factory class for user extension elements
    */

    private ExtensionElementFactory getFactory(String uri) throws SAXException {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash<0 || lastSlash==uri.length()-1) {
            return null;
        }
        String factoryClass = uri.substring(lastSlash+1);
        ExtensionElementFactory factory;

        try {
            return (ExtensionElementFactory)Loader.getInstance(factoryClass);
        } catch (Exception err) {
            return null;
        } 
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
